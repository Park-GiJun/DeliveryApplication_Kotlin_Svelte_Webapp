<script setup>
import { ref } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import { getWsBaseUrl } from '../utils/dateUtils';

// 라이더 정보 
const rider = ref({
  id: 'RIDER001',
  name: '김배달',
  status: 'AVAILABLE', // AVAILABLE, DELIVERING, OFFLINE
  currentOrder: null,
  location: {
    latitude: 37.5665,
    longitude: 126.9780
  }
});

// 주문 목록
const orders = ref([]);

// 상태 변경
const changeStatus = (newStatus) => {
  rider.value.status = newStatus;
};

// 위치 변경 시뮬레이션
const simulateLocationChange = () => {
  // 랜덤하게 위치 변경 (서울 근처)
  rider.value.location = {
    latitude: 37.5665 + (Math.random() - 0.5) * 0.01,
    longitude: 126.9780 + (Math.random() - 0.5) * 0.01
  };
};
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">라이더 앱</h1>
    
    <!-- 라이더 정보 -->
    <CardComponent title="라이더 정보">
      <div class="flex items-center mb-4">
        <div class="bg-blue-100 rounded-full p-3 mr-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
        </div>
        <div>
          <div class="text-lg font-semibold">{{ rider.name }}</div>
          <div class="text-sm text-gray-600">ID: {{ rider.id }}</div>
        </div>
      </div>
      
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="bg-gray-50 p-3 rounded">
          <div class="text-sm text-gray-500">상태</div>
          <div :class="[
            'font-medium',
            rider.status === 'AVAILABLE' ? 'text-green-600' : '',
            rider.status === 'DELIVERING' ? 'text-blue-600' : '',
            rider.status === 'OFFLINE' ? 'text-gray-600' : ''
          ]">
            {{ 
              rider.status === 'AVAILABLE' ? '배달 가능' : 
              rider.status === 'DELIVERING' ? '배달 중' : 
              '오프라인'
            }}
          </div>
        </div>
        
        <div class="bg-gray-50 p-3 rounded">
          <div class="text-sm text-gray-500">현재 위치</div>
          <div class="font-medium">
            {{ rider.location.latitude.toFixed(4) }}, {{ rider.location.longitude.toFixed(4) }}
          </div>
        </div>
      </div>
      
      <div class="flex space-x-2 mb-4">
        <button 
          @click="changeStatus('AVAILABLE')" 
          :class="[
            'btn flex-1 text-sm',
            rider.status === 'AVAILABLE' ? 'bg-green-600 text-white' : 'bg-gray-200'
          ]"
        >
          배달 가능
        </button>
        <button 
          @click="changeStatus('DELIVERING')" 
          :class="[
            'btn flex-1 text-sm',
            rider.status === 'DELIVERING' ? 'bg-blue-600 text-white' : 'bg-gray-200'
          ]"
        >
          배달 중
        </button>
        <button 
          @click="changeStatus('OFFLINE')" 
          :class="[
            'btn flex-1 text-sm',
            rider.status === 'OFFLINE' ? 'bg-gray-600 text-white' : 'bg-gray-200'
          ]"
        >
          오프라인
        </button>
      </div>
      
      <div class="flex justify-center">
        <button @click="simulateLocationChange" class="btn btn-primary">
          위치 변경 시뮬레이션
        </button>
      </div>
    </CardComponent>
    
    <!-- 배달 주문 -->
    <CardComponent title="배달 주문" class="mt-6">
      <div v-if="rider.currentOrder" class="bg-blue-50 p-4 rounded-lg mb-4">
        <div class="flex justify-between items-center mb-2">
          <div class="font-semibold">현재 배달</div>
          <div class="text-sm bg-blue-500 text-white px-2 py-1 rounded">진행 중</div>
        </div>
        
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <div class="text-sm text-gray-500">주문 번호</div>
            <div class="font-medium">{{ rider.currentOrder.id }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">금액</div>
            <div class="font-medium">₩{{ rider.currentOrder.amount.toLocaleString() }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">매장</div>
            <div class="font-medium">{{ rider.currentOrder.store }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">배달 시간</div>
            <div class="font-medium">{{ rider.currentOrder.estimatedTime }}분</div>
          </div>
        </div>
        
        <div class="bg-white p-3 rounded mb-4">
          <div class="text-sm text-gray-500 mb-1">배달 주소</div>
          <div>{{ rider.currentOrder.address }}</div>
        </div>
        
        <div class="flex space-x-3">
          <button class="btn bg-green-500 text-white flex-1">픽업 완료</button>
          <button class="btn bg-blue-500 text-white flex-1">배달 완료</button>
        </div>
      </div>
      
      <div v-else class="text-center py-12 text-gray-500">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto mb-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
        </svg>
        <p class="text-lg mb-2">현재 배달 중인 주문이 없습니다</p>
        <p class="text-sm">새로운 주문을 기다리는 중...</p>
      </div>
    </CardComponent>
    
    <!-- 웹소켓 연결 -->
    <div class="mt-6">
      <h2 class="text-xl font-bold mb-3">서버 연결</h2>
      <WebSocketConsole :url="`${getWsBaseUrl()}/ws/events`" />
    </div>
  </div>
</template>
