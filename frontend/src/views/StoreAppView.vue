<script setup>
import { ref, onMounted } from 'vue';
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
const handleOrderReceived = (orderData) => {
  console.log('새로운 주문 수신:', orderData);

  // 주문 정보를 주문 목록에 추가
  if (orderData) {
    // 새 주문 객체 생성
    const newOrder = {
      id: orderData.orderNumber,
      status: orderData.status,
      items: orderData.orderDetails?.items || [],
      totalPrice: orderData.totalAmount,
      deliveryFee: orderData.deliveryFee,
      timestamp: new Date()
    };

    // 주문 목록에 추가
    orders.value.unshift(newOrder);
  }
};

// 컴포넌트 마운트 시 매장 목록 가져오기
onMounted(() => {
  fetchStores();
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

    <!-- 웹소켓 연결 -->
    <div class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole
          :url="`${getWsBaseUrl()}/ws/store/1`"
          @message-received="handleOrderReceived"
      />
    </div>
  </div>
</template>