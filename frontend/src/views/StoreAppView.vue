<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import { getWsBaseUrl } from '../utils/dateUtils';
import axios from 'axios';

// 매장 정보
const store = ref({
  id: null,
  name: '',
  address: '',
  status: 'OPEN',
  category: '',
});

// 매장 목록
const storeList = ref([]);

// 선택된 매장 인덱스
const selectedStoreIndex = ref(0);

// 메뉴 목록
const menuItems = ref([]);

// 주문 목록
const orders = ref([]);

// 대기 중인 주문 목록
const pendingOrders = ref([]);

// 처리 중인 주문 목록
const processingOrders = ref([]);

// 주문 항목
const cartItems = ref([]);

// 총 주문 금액
const totalAmount = ref(0);

// 배달료
const deliveryFee = ref(3000);

// 매장 목록 가져오기
const fetchStores = async () => {
  try {
    const response = await axios.get('http://localhost:52001/api/stores');
    storeList.value = response.data;
    if (storeList.value.length > 0) {
      selectStore(0);
    }
  } catch (error) {
    console.error('매장 목록을 가져오는 중 오류 발생:', error);
  }
};

// 매장 선택
const selectStore = async (index) => {
  selectedStoreIndex.value = index;
  store.value = storeList.value[index];

  // 선택된 매장의 메뉴 가져오기
  try {
    const response = await axios.get(`http://localhost:52001/api/stores/${store.value.id}/menu`);
    menuItems.value = response.data;
  } catch (error) {
    console.error('메뉴를 가져오는 중 오류 발생:', error);
    menuItems.value = [];
  }

  // 장바구니 초기화
  cartItems.value = [];
  calculateTotal();
};

// 메뉴 추가
const addToCart = (menuItem) => {
  // 이미 장바구니에 있는지 확인
  const existingItem = cartItems.value.find(item => item.id === menuItem.id);

  if (existingItem) {
    // 이미 있으면 수량 증가
    existingItem.quantity += 1;
  } else {
    // 없으면 새로 추가
    cartItems.value.push({
      id: menuItem.id,
      name: menuItem.name,
      price: menuItem.price,
      quantity: 1
    });
  }

  calculateTotal();
};

// 장바구니에서 수량 변경
const updateQuantity = (itemId, change) => {
  const item = cartItems.value.find(item => item.id === itemId);
  if (item) {
    item.quantity += change;
    if (item.quantity <= 0) {
      // 수량이 0 이하면 제거
      cartItems.value = cartItems.value.filter(item => item.id !== itemId);
    }
    calculateTotal();
  }
};

// 총액 계산
const calculateTotal = () => {
  totalAmount.value = cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
};

// 주문하기
const placeOrder = async () => {
  if (cartItems.value.length === 0) {
    alert('메뉴를 선택해주세요');
    return;
  }

  // 주문 생성 객체
  const orderData = {
    customerId: 1, // 기본값으로 설정 (실제로는 로그인한 사용자 ID)
    storeId: store.value.id,
    addressId: 1, // 기본값으로 설정 (실제로는 선택한 주소 ID)
    items: cartItems.value.map(item => ({
      menuId: item.id,
      quantity: item.quantity,
      unitPrice: item.price,
      options: []
    })),
    requestStore: '',
    requestRider: ''
  };

  try {
    const response = await axios.post('http://localhost:52001/api/orders', orderData);
    alert('주문이 완료되었습니다!');

    // 장바구니 초기화
    cartItems.value = [];
    calculateTotal();

    // 주문 목록에 추가
    fetchOrders();
  } catch (error) {
    console.error('주문 중 오류 발생:', error);
    alert('주문 중 오류가 발생했습니다');
  }
};

// 주문 상태 텍스트
const getStatusText = (status) => {
  switch(status) {
    case 'CREATED': return '주문 접수됨';
    case 'ACCEPTED': return '주문 수락됨';
    case 'REJECTED': return '주문 거부됨';
    case 'READY_FOR_PICKUP': return '픽업 대기';
    case 'PICKED_UP': return '픽업 완료';
    case 'DELIVERED': return '배달 완료';
    case 'CANCELLED': return '주문 취소됨';
    default: return '알 수 없음';
  }
};

// 웹소켓을 통한 주문 수신 처리
const handleOrderReceived = (message) => {
  console.log('WebSocket 메시지 수신:', message);
  
  if (message.eventType === 'NEW_ORDER' || message.eventType === 'PENDING_ORDERS_UPDATE') {
    // 대기 중인 주문 목록 업데이트
    if (message.pendingOrders) {
      pendingOrders.value = message.pendingOrders.map(order => ({
        id: order.orderNumber,
        orderNumber: order.orderNumber,
        status: order.status,
        items: order.orderDetails?.items || [],
        totalAmount: order.totalAmount,
        deliveryFee: order.deliveryFee,
        payedAmount: order.payedAmount,
        customerName: order.orderDetails?.customerName || '고객',
        timestamp: new Date()
      }));
    }
    
    // 새 주문 알림 표시
    if (message.eventType === 'NEW_ORDER') {
      alert(`새로운 주문이 접수되었습니다! 주문번호: ${message.orderNumber}`);
    }
  } else if (message.eventType === 'ORDER_STATUS_CHANGED') {
    // 주문 상태 변경 처리
    updateOrderStatus(message.orderNumber, message.status);
  }
};

// 주문 상태 업데이트
const updateOrderStatus = (orderNumber, newStatus) => {
  // 대기 중인 주문에서 찾기
  const pendingOrderIndex = pendingOrders.value.findIndex(o => o.orderNumber === orderNumber);
  if (pendingOrderIndex !== -1) {
    const order = pendingOrders.value[pendingOrderIndex];
    order.status = newStatus;
    
    // 처리 중인 주문으로 이동
    if (newStatus === 'ACCEPTED' || newStatus === 'COOKING') {
      processingOrders.value.push(order);
      pendingOrders.value.splice(pendingOrderIndex, 1);
    }
  }
  
  // 처리 중인 주문에서 찾기
  const processingOrderIndex = processingOrders.value.findIndex(o => o.orderNumber === orderNumber);
  if (processingOrderIndex !== -1) {
    const order = processingOrders.value[processingOrderIndex];
    order.status = newStatus;
    
    // 완료된 주문은 목록에서 제거
    if (newStatus === 'DELIVERED' || newStatus === 'CANCELLED') {
      orders.value.unshift(order); // 주문 이력에 추가
      processingOrders.value.splice(processingOrderIndex, 1);
    }
  }
};

// 주문 수락
const acceptOrder = async (orderNumber) => {
  try {
    await axios.post(`http://localhost:52001/api/orders/${orderNumber}/status`, {
      status: 'ACCEPTED'
    });
    
    alert(`주문 ${orderNumber}이 수락되었습니다.`);
    updateOrderStatus(orderNumber, 'ACCEPTED');
  } catch (error) {
    console.error('주문 수락 중 오류:', error);
    alert('주문 수락 중 오류가 발생했습니다.');
  }
};

// 주문 거부
const rejectOrder = async (orderNumber) => {
  try {
    await axios.post(`http://localhost:52001/api/orders/${orderNumber}/status`, {
      status: 'REJECTED'
    });
    
    alert(`주문 ${orderNumber}이 거부되었습니다.`);
    updateOrderStatus(orderNumber, 'REJECTED');
  } catch (error) {
    console.error('주문 거부 중 오류:', error);
    alert('주문 거부 중 오류가 발생했습니다.');
  }
};

// 주문 준비 완료
const completeOrder = async (orderNumber) => {
  try {
    await axios.post(`http://localhost:52001/api/orders/${orderNumber}/status`, {
      status: 'READY'
    });
    
    alert(`주문 ${orderNumber}이 준비 완료되었습니다.`);
    updateOrderStatus(orderNumber, 'READY');
  } catch (error) {
    console.error('주문 준비 완료 처리 중 오류:', error);
    alert('주문 준비 완료 처리 중 오류가 발생했습니다.');
  }
};

// 주문 목록 조회
const fetchPendingOrders = async () => {
  if (!store.value.id) return;
  
  try {
    const response = await axios.get(`http://localhost:52001/api/orders/store/${store.value.id}`);
    pendingOrders.value = response.data.map(order => ({
      id: order.orderNumber,
      orderNumber: order.orderNumber,
      status: order.status,
      items: order.orderDetails?.items || [],
      totalAmount: order.totalAmount,
      deliveryFee: order.deliveryFee,
      payedAmount: order.payedAmount,
      customerName: order.orderDetails?.customerName || '고객',
      timestamp: new Date()
    }));
  } catch (error) {
    console.error('주문 목록 조회 중 오류:', error);
  }
};

// 컴포넌트 마운트 시 초기화
onMounted(() => {
  fetchStores();
  
  // 30초마다 주문 목록 갱신 (폴백용)
  const interval = setInterval(() => {
    if (store.value.id) {
      fetchPendingOrders();
    }
  }, 30000);
  
  // 컴포넌트 언마운트 시 인터벌 정리
  onUnmounted(() => {
    clearInterval(interval);
  });
});
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">매장 앱</h1>

    <!-- 매장 탭 -->
    <div class="mb-6">
      <div class="flex overflow-x-auto space-x-1 border-b">
        <button
            v-for="(storeItem, index) in storeList"
            :key="storeItem.id"
            @click="selectStore(index)"
            :class="[
            'px-4 py-2 whitespace-nowrap',
            selectedStoreIndex === index
              ? 'bg-blue-500 text-white rounded-t'
              : 'bg-gray-100 hover:bg-gray-200 rounded-t'
          ]"
        >
          {{ storeItem.name }}
        </button>
      </div>
    </div>

    <!-- 선택된 매장 정보 -->
    <CardComponent v-if="store.id" title="매장 정보">
      <div class="flex justify-between items-center mb-4">
        <div>
          <div class="text-lg font-semibold">{{ store.name }}</div>
          <div class="text-sm text-gray-600">카테고리: {{ store.category }}</div>
        </div>
        <div :class="[
          'px-3 py-1 rounded-full text-white text-sm',
          store.status === 'OPEN' ? 'bg-green-500' : '',
          store.status === 'CLOSED' ? 'bg-red-500' : '',
          store.status === 'BREAK' ? 'bg-yellow-500' : ''
        ]">
          {{
            store.status === 'OPEN' ? '영업중' :
                store.status === 'CLOSED' ? '영업종료' :
                    '휴식중'
          }}
        </div>
      </div>

      <div class="bg-gray-50 p-3 rounded">
        <div class="text-sm text-gray-500">주소</div>
        <div class="font-medium">{{ store.address }}</div>
      </div>
    </CardComponent>

    <!-- 메뉴 목록 -->
    <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 메뉴 섹션 -->
      <CardComponent title="메뉴 목록">
        <div v-if="menuItems.length === 0" class="text-center py-6 text-gray-500">
          메뉴 정보가 없습니다
        </div>
        <div v-else class="space-y-4">
          <div v-for="item in menuItems" :key="item.id" class="border rounded-lg p-4">
            <div class="flex justify-between items-start">
              <div>
                <div class="font-medium">{{ item.name }}</div>
                <div class="text-sm text-gray-600 mt-1">{{ item.description }}</div>
                <div class="text-sm font-medium mt-2">{{ item.price.toLocaleString() }}원</div>
              </div>
              <button
                  @click="addToCart(item)"
                  class="bg-blue-500 text-white px-2 py-1 rounded text-sm"
                  :disabled="!item.available"
              >
                {{ item.available ? '주문하기' : '품절' }}
              </button>
            </div>
          </div>
        </div>
      </CardComponent>

      <!-- 장바구니 섹션 -->
      <CardComponent title="주문 장바구니">
        <div v-if="cartItems.length === 0" class="text-center py-6 text-gray-500">
          장바구니가 비어있습니다
        </div>
        <div v-else>
          <div v-for="item in cartItems" :key="item.id" class="flex justify-between items-center mb-4 border-b pb-2">
            <div>
              <div class="font-medium">{{ item.name }}</div>
              <div class="text-sm">{{ item.price.toLocaleString() }}원</div>
            </div>
            <div class="flex items-center">
              <button @click="updateQuantity(item.id, -1)" class="w-8 h-8 bg-gray-200 rounded-l flex items-center justify-center">-</button>
              <div class="w-8 h-8 bg-gray-100 flex items-center justify-center">{{ item.quantity }}</div>
              <button @click="updateQuantity(item.id, 1)" class="w-8 h-8 bg-gray-200 rounded-r flex items-center justify-center">+</button>
            </div>
          </div>

          <div class="border-t pt-3">
            <div class="flex justify-between mb-1">
              <div>상품 합계</div>
              <div>{{ totalAmount.toLocaleString() }}원</div>
            </div>
            <div class="flex justify-between mb-1">
              <div>배달료</div>
              <div>{{ deliveryFee.toLocaleString() }}원</div>
            </div>
            <div class="flex justify-between font-bold">
              <div>총 결제금액</div>
              <div>{{ (totalAmount + deliveryFee).toLocaleString() }}원</div>
            </div>
          </div>

          <button @click="placeOrder" class="mt-4 bg-blue-500 text-white w-full py-2 rounded font-medium">
            주문하기
          </button>
        </div>
      </CardComponent>
    </div>

    <!-- 대기 중인 주문 목록 -->
    <CardComponent title="대기 중인 주문" class="mt-6">
      <div v-if="pendingOrders.length === 0" class="text-center py-6 text-gray-500">
        대기 중인 주문이 없습니다
      </div>
      <div v-else class="space-y-4">
        <div v-for="order in pendingOrders" :key="order.orderNumber" class="border rounded-lg p-4">
          <div class="flex justify-between items-start mb-3">
            <div>
              <div class="font-medium">주문번호: {{ order.orderNumber }}</div>
              <div class="text-sm text-gray-600">주문시간: {{ new Date(order.timestamp).toLocaleTimeString() }}</div>
            </div>
            <div class="bg-yellow-100 text-yellow-800 px-2 py-1 rounded text-sm">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="bg-gray-50 p-3 rounded mb-3">
            <div class="text-sm font-medium mb-2">주문 내역</div>
            <div v-if="order.items && order.items.length > 0" class="space-y-2">
              <div v-for="(item, idx) in order.items" :key="idx" class="flex justify-between">
                <div>{{ item.menuName }} x{{ item.quantity }}</div>
                <div>{{ item.totalPrice.toLocaleString() }}원</div>
              </div>
            </div>
            <div v-else class="text-sm text-gray-500">주문 상품 정보가 없습니다</div>
            
            <div class="border-t mt-2 pt-2">
              <div class="flex justify-between font-medium">
                <div>총 금액</div>
                <div>{{ order.payedAmount.toLocaleString() }}원</div>
              </div>
            </div>
          </div>
          
          <div class="flex space-x-3">
            <button @click="acceptOrder(order.orderNumber)" class="btn bg-green-500 text-white flex-1">
              주문 수락
            </button>
            <button @click="rejectOrder(order.orderNumber)" class="btn bg-red-500 text-white flex-1">
              주문 거부
            </button>
          </div>
        </div>
      </div>
    </CardComponent>
    
    <!-- 처리 중인 주문 -->
    <CardComponent title="처리 중인 주문" class="mt-6">
      <div v-if="processingOrders.length === 0" class="text-center py-6 text-gray-500">
        처리 중인 주문이 없습니다
      </div>
      <div v-else class="space-y-4">
        <div v-for="order in processingOrders" :key="order.orderNumber" class="border rounded-lg p-4">
          <div class="flex justify-between items-start mb-3">
            <div>
              <div class="font-medium">주문번호: {{ order.orderNumber }}</div>
              <div class="text-sm text-gray-600">주문시간: {{ new Date(order.timestamp).toLocaleTimeString() }}</div>
            </div>
            <div class="bg-blue-100 text-blue-800 px-2 py-1 rounded text-sm">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="bg-gray-50 p-3 rounded mb-3">
            <div class="text-sm font-medium mb-2">주문 내역</div>
            <div v-if="order.items && order.items.length > 0" class="space-y-2">
              <div v-for="(item, idx) in order.items" :key="idx" class="flex justify-between">
                <div>{{ item.menuName }} x{{ item.quantity }}</div>
                <div>{{ item.totalPrice.toLocaleString() }}원</div>
              </div>
            </div>
            <div v-else class="text-sm text-gray-500">주문 상품 정보가 없습니다</div>
          </div>
          
          <div class="flex justify-center">
            <button v-if="order.status === 'ACCEPTED' || order.status === 'COOKING'" 
                    @click="completeOrder(order.orderNumber)" 
                    class="btn bg-green-500 text-white w-full">
              준비 완료
            </button>
            <div v-else-if="order.status === 'READY'" class="text-center py-2 bg-gray-100 rounded w-full">
              라이더 픽업 대기 중
            </div>
          </div>
        </div>
      </div>
    </CardComponent>

    <!-- 웹소켓 연결 -->
    <div class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole
          :url="`${getWsBaseUrl()}/ws/store/${store.id || 1}`"
          @message-received="handleOrderReceived"
      />
    </div>
  </div>
</template>