<script setup>
import { ref, onMounted } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import { getWsBaseUrl } from '../utils/dateUtils';
import axios from 'axios';

// 고객 목록
const customers = ref([]);
// 선택된 고객 인덱스
const selectedCustomerIndex = ref(0);
// 선택된 고객
const selectedCustomer = ref(null);

// 활성 주문
const activeOrder = ref(null);

// 가게 목록
const stores = ref([]);
// 메뉴 목록
const menuItems = ref([]);
// 주문 생성 상태
const orderForm = ref({
  storeId: 1,
  storeName: '맛있는 치킨',
  items: [],
  totalPrice: 0,
  deliveryFee: 3000
});

// 가게 목록 불러오기
const fetchStores = async () => {
  try {
    const response = await axios.get('http://localhost:52001/api/test/data/stores');
    stores.value = response.data;
    if (stores.value.length > 0) {
      selectStore(stores.value[0]);
    }
  } catch (error) {
    console.error('가게 목록을 불러오는데 실패했습니다:', error);
  }
};

// 메뉴 목록 불러오기
const fetchMenuItems = async () => {
  try {
    const response = await axios.get('http://localhost:52001/api/test/menus');
    menuItems.value = response.data;
    
    // 기본 메뉴 2개 장바구니에 추가
    if (menuItems.value.length > 0) {
      orderForm.value.items = [
        { 
          id: menuItems.value[0].id, 
          name: menuItems.value[0].name, 
          price: menuItems.value[0].price, 
          quantity: 1 
        },
        {
          id: menuItems.value[4].id,
          name: menuItems.value[4].name,
          price: menuItems.value[4].price,
          quantity: 1
        }
      ];
      calculateTotalPrice();
    }
  } catch (error) {
    console.error('메뉴 목록을 불러오는데 실패했습니다:', error);
  }
};

// 가게 선택
const selectStore = (store) => {
  orderForm.value.storeId = store.id;
  orderForm.value.storeName = store.name;
};

// 총 가격 계산
const calculateTotalPrice = () => {
  orderForm.value.totalPrice = orderForm.value.items.reduce(
    (sum, item) => sum + (item.price * item.quantity), 0
  );
};

// 고객 목록 가져오기
const fetchCustomers = async () => {
  try {
    // 테스트용 고객 데이터 API 사용
    const response = await axios.get('http://localhost:52001/api/test/data/customers');
    customers.value = response.data;
    
    if (customers.value.length > 0) {
      // 저장된 고객 인덱스가 있으면 사용, 없으면 첫 번째 고객 선택
      const savedIndex = localStorage.getItem('selectedCustomerIndex');
      const indexToSelect = savedIndex !== null ? 
        parseInt(savedIndex) : 0;
      
      selectCustomer(indexToSelect < customers.value.length ? indexToSelect : 0);
    }
  } catch (error) {
    console.error('고객 목록을 가져오는 중 오류 발생:', error);
    
    // 백업 고객 데이터 사용
    customers.value = [
      { id: 1, name: '김민준', email: 'kim.minjun@example.com', phone: '010-1111-1111' },
      { id: 2, name: '이서연', email: 'lee.seoyeon@example.com', phone: '010-2222-2222' },
      { id: 3, name: '박지훈', email: 'park.jihoon@example.com', phone: '010-3333-3333' }
    ];
    selectCustomer(0);
  }
};

// 고객 선택
const selectCustomer = (index) => {
  selectedCustomerIndex.value = index;
  selectedCustomer.value = customers.value[index];
  // 주문 정보 초기화
  activeOrder.value = null;
  
  // 로컬 스토리지에 선택한 고객 정보 저장 (페이지 이동 후에도 유지)
  localStorage.setItem('selectedCustomerIndex', index);
  
  console.log(`고객 선택: ${selectedCustomer.value.name} (ID: ${selectedCustomer.value.id})`);
};

// 주문하기
const placeOrder = async () => {
  try {
    const orderData = {
      customerId: selectedCustomer.value.id,
      storeId: 1, // 예시 데이터 (실제 서비스에서는 사용자가 선택한 매장 ID)
      addressId: 1, // 예시 데이터 (실제 서비스에서는 사용자가 선택한 주소 ID)
      items: orderForm.value.items.map(item => ({
        menuId: Number(item.id.toString().replace('ITEM', '')),
        quantity: item.quantity,
        unitPrice: item.price,
        options: [] // 필요시 옵션도 추가
      })),
      requestStore: "고객앱에서 전송한 주문입니다",
      requestRider: "문 앞에 놓아주세요"
    };
    
    console.log("주문 요청 데이터:", orderData);
    
    // 백엔드 API에 주문 전송
    const response = await axios.post('http://localhost:52001/api/orders', orderData);
    console.log("주문 응답:", response.data);
    
    // 주문 성공 시 activeOrder에 저장
    activeOrder.value = {
      id: response.data.orderNumber,
      customerId: selectedCustomer.value.id,
      customerName: selectedCustomer.value.name,
      customerAddress: '서울시 강남구', // 예시 데이터
      storeId: orderForm.value.storeId,
      storeName: orderForm.value.storeName,
      items: [...orderForm.value.items],
      totalPrice: orderForm.value.totalPrice,
      deliveryFee: orderForm.value.deliveryFee,
      status: response.data.status,
      createdAt: new Date(),
      riderId: null,
      estimatedDeliveryTime: response.data.estimatedDeliveryMinutes
    };
    
    alert('주문이 완료되었습니다! 주문번호: ' + response.data.orderNumber);
  } catch (error) {
    console.error('주문 처리 중 오류가 발생했습니다:', error);
    alert('주문 처리 중 오류가 발생했습니다: ' + (error.response?.data?.message || error.message));
  }
};

// 주문 취소
const cancelOrder = async () => {
  if (!activeOrder.value || !activeOrder.value.id) return;
  
  try {
    await axios.post(`http://localhost:52001/api/orders/${activeOrder.value.id}/cancel`, {
      reason: "고객 요청에 의한 취소"
    });
    
    activeOrder.value.status = 'CANCELLED';
    setTimeout(() => {
      activeOrder.value = null;
    }, 3000);
    
    alert('주문이 취소되었습니다.');
  } catch (error) {
    console.error('주문 취소 중 오류가 발생했습니다:', error);
    alert('주문 취소 중 오류가 발생했습니다: ' + (error.response?.data?.message || error.message));
  }
};

// WebSocket 메시지 처리
const handleWebSocketMessage = (message) => {
  console.log('WebSocket 메시지 수신:', message);
  
  // 주문 상태 업데이트 처리
  if (message.eventType && message.eventType.startsWith('ORDER_') && message.data) {
    const orderData = message.data;
    
    // 현재 활성화된 주문이고, 주문 번호가 일치하는 경우에만 처리
    if (activeOrder.value && activeOrder.value.id === orderData.orderNumber) {
      // 주문 상태 업데이트
      activeOrder.value.status = orderData.status;
      
      // 상태에 따른 추가 처리
      if (orderData.status === 'ASSIGNED' && orderData.riderId) {
        activeOrder.value.riderId = orderData.riderId;
      }
      
      if (orderData.status === 'DELIVERED') {
        // 배달 완료 시 3초 후 주문 화면 초기화
        setTimeout(() => {
          activeOrder.value = null;
        }, 3000);
      }
    }
  }
};

// 주문 상태 텍스트
const getStatusText = (status) => {
  switch(status) {
    case 'CREATED': return '주문 접수됨';
    case 'ASSIGNED': return '라이더 배정됨';
    case 'PICKED_UP': return '픽업 완료';
    case 'DELIVERED': return '배달 완료';
    case 'CANCELLED': return '주문 취소됨';
    default: return '알 수 없음';
  }
};

// 컴포넌트 마운트 시 필요한 데이터 가져오기
onMounted(() => {
  fetchCustomers();
  fetchStores();
  fetchMenuItems();
});
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">고객 앱</h1>
    
    <!-- 고객 탭 -->
    <div class="mb-6">
      <div class="flex overflow-x-auto space-x-1 border-b">
        <button 
          v-for="(customer, index) in customers" 
          :key="customer.id"
          @click="selectCustomer(index)"
          :class="[
            'px-4 py-2 whitespace-nowrap',
            selectedCustomerIndex === index 
              ? 'bg-blue-500 text-white rounded-t' 
              : 'bg-gray-100 hover:bg-gray-200 rounded-t'
          ]"
        >
          {{ customer.name }}
        </button>
      </div>
    </div>
    
    <!-- 선택된 고객 정보 -->
    <CardComponent v-if="selectedCustomer" title="고객 정보">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <div class="text-sm text-gray-500">이름</div>
          <div class="font-medium">{{ selectedCustomer.name }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">ID</div>
          <div class="font-medium">{{ selectedCustomer.id }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">이메일</div>
          <div class="font-medium">{{ selectedCustomer.email }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">연락처</div>
          <div class="font-medium">{{ selectedCustomer.phone }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">계정 상태</div>
          <div class="font-medium">{{ selectedCustomer.activatedStatus }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">가입일</div>
          <div class="font-medium">{{ selectedCustomer.createdAt ? `${selectedCustomer.createdAt[0]}-${selectedCustomer.createdAt[1]}-${selectedCustomer.createdAt[2]}` : '' }}</div>
        </div>
      </div>
    </CardComponent>
    
    <!-- 활성 주문 -->
    <CardComponent v-if="activeOrder" title="현재 주문" class="mt-6">
      <div class="bg-blue-50 p-4 rounded-lg mb-4">
        <div class="flex justify-between items-center mb-2">
          <div class="font-semibold">{{ activeOrder.storeName }}</div>
          <div :class="[
            'text-sm px-2 py-1 rounded',
            activeOrder.status === 'CREATED' ? 'bg-yellow-500 text-white' : '',
            activeOrder.status === 'ASSIGNED' ? 'bg-blue-500 text-white' : '',
            activeOrder.status === 'PICKED_UP' ? 'bg-purple-500 text-white' : '',
            activeOrder.status === 'DELIVERED' ? 'bg-green-500 text-white' : '',
            activeOrder.status === 'CANCELLED' ? 'bg-red-500 text-white' : ''
          ]">
            {{ getStatusText(activeOrder.status) }}
          </div>
        </div>
        
        <div class="bg-white p-3 rounded mb-4">
          <div class="text-sm font-medium mb-2">주문 상품</div>
          <div class="space-y-2">
            <div v-for="item in activeOrder.items" :key="item.id" class="flex justify-between">
              <div>{{ item.name }} x{{ item.quantity }}</div>
              <div>{{ item.price.toLocaleString() }}원</div>
            </div>
            <div class="border-t pt-2 flex justify-between font-medium">
              <div>상품 합계</div>
              <div>{{ activeOrder.totalPrice.toLocaleString() }}원</div>
            </div>
            <div class="flex justify-between">
              <div>배달료</div>
              <div>{{ activeOrder.deliveryFee.toLocaleString() }}원</div>
            </div>
            <div class="border-t pt-2 flex justify-between font-bold">
              <div>총 결제금액</div>
              <div>{{ (activeOrder.totalPrice + activeOrder.deliveryFee).toLocaleString() }}원</div>
            </div>
          </div>
        </div>
        
        <div v-if="activeOrder.riderId" class="bg-white p-3 rounded mb-4">
          <div class="text-sm font-medium mb-2">배달 정보</div>
          <div class="space-y-2">
            <div class="flex justify-between">
              <div class="text-sm text-gray-500">라이더</div>
              <div>{{ activeOrder.riderId }}</div>
            </div>
            <div class="flex justify-between">
              <div class="text-sm text-gray-500">예상 배달 시간</div>
              <div>{{ activeOrder.estimatedDeliveryTime || '배정 대기 중' }}</div>
            </div>
          </div>
        </div>
        
        <div class="flex justify-center">
          <button @click="cancelOrder" class="btn bg-red-500 text-white" 
                 :disabled="activeOrder.status !== 'CREATED'">
            주문 취소
          </button>
        </div>
      </div>
    </CardComponent>
    
    <!-- 주문하기 -->
    <CardComponent v-if="!activeOrder && selectedCustomer" title="주문하기" class="mt-6">
      <div class="bg-white p-4 rounded-lg border">
        <div class="flex justify-between items-center mb-3">
          <div class="font-semibold">{{ orderForm.storeName }}</div>
          <div class="text-sm text-gray-500">
            <select v-if="stores.length > 0" v-model="orderForm.storeId" @change="store => selectStore(stores.find(s => s.id === store.target.value))" class="border rounded p-1">
              <option v-for="store in stores" :key="store.id" :value="store.id">{{ store.name }}</option>
            </select>
          </div>
        </div>
        
        <div class="space-y-3 mb-4">
          <div v-for="(item, index) in orderForm.items" :key="index" class="flex justify-between items-center border-b pb-2">
            <div>
              <div>{{ item.name }}</div>
              <div class="text-sm text-gray-500">{{ item.price.toLocaleString() }}원</div>
            </div>
            <div class="flex items-center">
              <button class="w-8 h-8 bg-gray-200 rounded-l flex items-center justify-center" 
                      @click="() => { if(item.quantity > 1) { item.quantity--; calculateTotalPrice(); } }">-</button>
              <div class="w-8 h-8 bg-gray-100 flex items-center justify-center">{{ item.quantity }}</div>
              <button class="w-8 h-8 bg-gray-200 rounded-r flex items-center justify-center" 
                      @click="() => { item.quantity++; calculateTotalPrice(); }">+</button>
            </div>
          </div>
        </div>
        
        <!-- 메뉴 추가 -->
        <div class="mb-4">
          <div class="text-sm font-medium mb-2">메뉴 추가</div>
          <div class="grid grid-cols-2 gap-2">
            <div v-for="menu in menuItems" :key="menu.id" 
                class="border rounded p-2 text-sm cursor-pointer hover:bg-gray-50"
                @click="() => { 
                  const existingItem = orderForm.items.find(i => i.id === menu.id);
                  if (existingItem) {
                    existingItem.quantity++;
                  } else {
                    orderForm.items.push({ id: menu.id, name: menu.name, price: menu.price, quantity: 1 });
                  }
                  calculateTotalPrice();
                }">
              <div class="font-medium">{{ menu.name }}</div>
              <div class="text-gray-500">{{ menu.price.toLocaleString() }}원</div>
            </div>
          </div>
        </div>
        
        <div class="border-t pt-3 mb-4">
          <div class="flex justify-between mb-1">
            <div>상품 합계</div>
            <div>{{ orderForm.totalPrice.toLocaleString() }}원</div>
          </div>
          <div class="flex justify-between mb-1">
            <div>배달료</div>
            <div>{{ orderForm.deliveryFee.toLocaleString() }}원</div>
          </div>
          <div class="flex justify-between font-bold">
            <div>총 결제금액</div>
            <div>{{ (orderForm.totalPrice + orderForm.deliveryFee).toLocaleString() }}원</div>
          </div>
        </div>
        
        <div class="flex justify-center">
          <button @click="placeOrder" class="btn bg-blue-500 text-white w-full">
            주문하기
          </button>
        </div>
      </div>
    </CardComponent>
    
    <!-- 웹소켓 연결 -->
    <div v-if="selectedCustomer" class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole 
        :url="`${getWsBaseUrl()}/ws/customer/${selectedCustomer.id}`"
        @message-received="handleWebSocketMessage" 
      />
    </div>
  </div>
</template>