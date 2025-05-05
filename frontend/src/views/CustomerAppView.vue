<script setup>
import { ref } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import { getWsBaseUrl } from '../utils/dateUtils';

// 고객 정보
const customer = ref({
  id: 'CUST001',
  name: '홍길동',
  address: '서울시 강남구 테헤란로 123',
  phone: '010-1234-5678'
});

// 활성 주문
const activeOrder = ref(null);

// 주문 생성 상태
const orderForm = ref({
  storeId: 'STORE001',
  storeName: '맛있는 치킨',
  items: [
    { id: 'ITEM001', name: '후라이드 치킨', price: 18000, quantity: 1 },
    { id: 'ITEM002', name: '콜라', price: 2000, quantity: 1 }
  ],
  totalPrice: 20000,
  deliveryFee: 3000
});

// 주문하기
const placeOrder = () => {
  // 여기서 실제로는 서버에 주문을 전송하게 됩니다
  activeOrder.value = {
    id: 'ORDER' + Date.now().toString().slice(-6),
    customerId: customer.value.id,
    customerName: customer.value.name,
    customerAddress: customer.value.address,
    storeId: orderForm.value.storeId,
    storeName: orderForm.value.storeName,
    items: [...orderForm.value.items],
    totalPrice: orderForm.value.totalPrice,
    deliveryFee: orderForm.value.deliveryFee,
    status: 'CREATED',
    createdAt: new Date(),
    riderId: null,
    estimatedDeliveryTime: null
  };
};

// 주문 취소
const cancelOrder = () => {
  activeOrder.value = null;
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
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">고객 앱</h1>
    
    <!-- 고객 정보 -->
    <CardComponent title="고객 정보">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <div class="text-sm text-gray-500">이름</div>
          <div class="font-medium">{{ customer.name }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">ID</div>
          <div class="font-medium">{{ customer.id }}</div>
        </div>
        <div class="col-span-2">
          <div class="text-sm text-gray-500">주소</div>
          <div class="font-medium">{{ customer.address }}</div>
        </div>
        <div class="col-span-2">
          <div class="text-sm text-gray-500">연락처</div>
          <div class="font-medium">{{ customer.phone }}</div>
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
    <CardComponent v-if="!activeOrder" title="주문하기" class="mt-6">
      <div class="bg-white p-4 rounded-lg border">
        <div class="font-semibold mb-3">{{ orderForm.storeName }}</div>
        
        <div class="space-y-3 mb-4">
          <div v-for="(item, index) in orderForm.items" :key="index" class="flex justify-between items-center">
            <div>
              <div>{{ item.name }}</div>
              <div class="text-sm text-gray-500">{{ item.price.toLocaleString() }}원</div>
            </div>
            <div class="flex items-center">
              <button class="w-8 h-8 bg-gray-200 rounded-l flex items-center justify-center"
                      @click="item.quantity > 1 ? item.quantity-- : null">-</button>
              <div class="w-8 h-8 bg-gray-100 flex items-center justify-center">{{ item.quantity }}</div>
              <button class="w-8 h-8 bg-gray-200 rounded-r flex items-center justify-center"
                      @click="item.quantity++">+</button>
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
    <div class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole :url="`${getWsBaseUrl()}/ws/events`" />
    </div>
  </div>
</template>
