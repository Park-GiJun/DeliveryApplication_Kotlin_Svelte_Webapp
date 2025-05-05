<script setup>
import { ref } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import { getWsBaseUrl } from '../utils/dateUtils';

// 매장 정보
const store = ref({
  id: 'STORE001',
  name: '맛있는 치킨',
  address: '서울시 강남구 역삼로 123',
  phone: '02-123-4567',
  status: 'OPEN' // OPEN, CLOSED, BREAK
});

// 주문 목록
const orders = ref([
  {
    id: 'ORDER123456',
    customerId: 'CUST001',
    customerName: '홍길동',
    customerAddress: '서울시 강남구 테헤란로 123, 101동 1505호',
    status: 'CREATED',
    createdAt: new Date(Date.now() - 5 * 60000), // 5분 전
    items: [
      { id: 'ITEM001', name: '후라이드 치킨', price: 18000, quantity: 1 },
      { id: 'ITEM002', name: '콜라', price: 2000, quantity: 1 }
    ],
    totalPrice: 20000,
    deliveryFee: 3000,
    riderId: null
  }
]);

// 메뉴 목록
const menuItems = ref([
  { id: 'ITEM001', name: '후라이드 치킨', price: 18000, available: true },
  { id: 'ITEM002', name: '콜라', price: 2000, available: true },
  { id: 'ITEM003', name: '양념 치킨', price: 19000, available: true },
  { id: 'ITEM004', name: '간장 치킨', price: 19000, available: true },
  { id: 'ITEM005', name: '반반 치킨', price: 19000, available: false },
]);

// 주문 수락
const acceptOrder = (orderId) => {
  const orderIndex = orders.value.findIndex(order => order.id === orderId);
  if (orderIndex !== -1) {
    orders.value[orderIndex].status = 'ACCEPTED';
  }
};

// 주문 거부
const rejectOrder = (orderId) => {
  const orderIndex = orders.value.findIndex(order => order.id === orderId);
  if (orderIndex !== -1) {
    orders.value[orderIndex].status = 'REJECTED';
  }
};

// 주문 완료 (조리 완료)
const completeOrder = (orderId) => {
  const orderIndex = orders.value.findIndex(order => order.id === orderId);
  if (orderIndex !== -1) {
    orders.value[orderIndex].status = 'READY_FOR_PICKUP';
  }
};

// 메뉴 상태 변경
const toggleMenuAvailability = (itemId) => {
  const menuIndex = menuItems.value.findIndex(item => item.id === itemId);
  if (menuIndex !== -1) {
    menuItems.value[menuIndex].available = !menuItems.value[menuIndex].available;
  }
};

// 매장 상태 변경
const changeStoreStatus = (status) => {
  store.value.status = status;
};

// 주문 상태 텍스트
const getStatusText = (status) => {
  switch(status) {
    case 'CREATED': return '신규 주문';
    case 'ACCEPTED': return '주문 수락됨';
    case 'REJECTED': return '주문 거부됨';
    case 'READY_FOR_PICKUP': return '픽업 대기';
    case 'PICKED_UP': return '픽업 완료';
    case 'DELIVERED': return '배달 완료';
    case 'CANCELLED': return '주문 취소됨';
    default: return '알 수 없음';
  }
};

// 주문 경과 시간
const getElapsedTime = (createdAt) => {
  const elapsed = Math.floor((Date.now() - new Date(createdAt).getTime()) / 60000);
  return elapsed < 60 ? `${elapsed}분 전` : `${Math.floor(elapsed/60)}시간 ${elapsed%60}분 전`;
};
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">매장 앱</h1>
    
    <!-- 매장 정보 -->
    <CardComponent title="매장 정보">
      <div class="flex justify-between items-center mb-4">
        <div>
          <div class="text-lg font-semibold">{{ store.name }}</div>
          <div class="text-sm text-gray-600">ID: {{ store.id }}</div>
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
      
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-gray-50 p-3 rounded">
          <div class="text-sm text-gray-500">주소</div>
          <div class="font-medium">{{ store.address }}</div>
        </div>
        <div class="bg-gray-50 p-3 rounded">
          <div class="text-sm text-gray-500">연락처</div>
          <div class="font-medium">{{ store.phone }}</div>
        </div>
      </div>
      
      <div class="flex space-x-2">
        <button 
          @click="changeStoreStatus('OPEN')" 
          :class="[
            'btn flex-1 text-sm',
            store.status === 'OPEN' ? 'bg-green-600 text-white' : 'bg-gray-200'
          ]"
        >
          영업 시작
        </button>
        <button 
          @click="changeStoreStatus('BREAK')" 
          :class="[
            'btn flex-1 text-sm',
            store.status === 'BREAK' ? 'bg-yellow-600 text-white' : 'bg-gray-200'
          ]"
        >
          휴식 시간
        </button>
        <button 
          @click="changeStoreStatus('CLOSED')" 
          :class="[
            'btn flex-1 text-sm',
            store.status === 'CLOSED' ? 'bg-red-600 text-white' : 'bg-gray-200'
          ]"
        >
          영업 종료
        </button>
      </div>
    </CardComponent>
    
    <!-- 주문 목록 -->
    <CardComponent title="주문 목록" class="mt-6">
      <div v-if="orders.length === 0" class="text-center py-12 text-gray-500">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto mb-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        <p class="text-lg mb-2">주문이 없습니다</p>
        <p class="text-sm">새로운 주문이 들어오면 여기에 표시됩니다</p>
      </div>
      
      <div v-else class="space-y-4">
        <div v-for="order in orders" :key="order.id" 
             :class="[
               'border rounded-lg overflow-hidden',
               order.status === 'CREATED' ? 'border-yellow-500' : '',
               order.status === 'ACCEPTED' ? 'border-blue-500' : '',
               order.status === 'READY_FOR_PICKUP' ? 'border-green-500' : '',
               order.status === 'REJECTED' || order.status === 'CANCELLED' ? 'border-red-500' : ''
             ]">
          <div :class="[
            'px-4 py-2 flex justify-between items-center',
            order.status === 'CREATED' ? 'bg-yellow-100' : '',
            order.status === 'ACCEPTED' ? 'bg-blue-100' : '',
            order.status === 'READY_FOR_PICKUP' ? 'bg-green-100' : '',
            order.status === 'REJECTED' || order.status === 'CANCELLED' ? 'bg-red-100' : ''
          ]">
            <div>
              <span class="font-medium">주문 #{{ order.id.slice(-6) }}</span>
              <span class="text-sm text-gray-600 ml-2">{{ getElapsedTime(order.createdAt) }}</span>
            </div>
            <div :class="[
              'px-2 py-1 rounded text-white text-sm',
              order.status === 'CREATED' ? 'bg-yellow-500' : '',
              order.status === 'ACCEPTED' ? 'bg-blue-500' : '',
              order.status === 'READY_FOR_PICKUP' ? 'bg-green-500' : '',
              order.status === 'REJECTED' || order.status === 'CANCELLED' ? 'bg-red-500' : ''
            ]">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="p-4">
            <div class="mb-3">
              <div class="font-medium">{{ order.customerName }}</div>
              <div class="text-sm text-gray-600">{{ order.customerAddress }}</div>
            </div>
            
            <div class="space-y-2 mb-3">
              <div v-for="item in order.items" :key="item.id" class="flex justify-between">
                <div>{{ item.name }} x{{ item.quantity }}</div>
                <div>{{ item.price.toLocaleString() }}원</div>
              </div>
              <div class="border-t pt-2 flex justify-between font-medium">
                <div>합계</div>
                <div>{{ (order.totalPrice + order.deliveryFee).toLocaleString() }}원</div>
              </div>
            </div>
            
            <div class="flex space-x-2" v-if="order.status === 'CREATED'">
              <button @click="acceptOrder(order.id)" class="btn btn-success flex-1">주문 수락</button>
              <button @click="rejectOrder(order.id)" class="btn btn-danger flex-1">주문 거부</button>
            </div>
            
            <div class="flex" v-if="order.status === 'ACCEPTED'">
              <button @click="completeOrder(order.id)" class="btn btn-primary w-full">조리 완료</button>
            </div>
          </div>
        </div>
      </div>
    </CardComponent>
    
    <!-- 메뉴 관리 -->
    <CardComponent title="메뉴 관리" class="mt-6">
      <div class="overflow-hidden border rounded-lg">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                메뉴
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                가격
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in menuItems" :key="item.id">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="font-medium text-gray-900">{{ item.name }}</div>
                <div class="text-sm text-gray-500">{{ item.id }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ item.price.toLocaleString() }}원
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="[
                  'px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full',
                  item.available ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]">
                  {{ item.available ? '판매중' : '품절' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button @click="toggleMenuAvailability(item.id)" 
                        :class="item.available ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'">
                  {{ item.available ? '품절로 변경' : '판매 시작' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </CardComponent>
    
    <!-- 웹소켓 연결 -->
    <div class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole :url="`${getWsBaseUrl()}/ws/events`" />
    </div>
  </div>
</template>
