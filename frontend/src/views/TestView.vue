<script setup>
import { ref, reactive, onMounted } from 'vue';
import CardComponent from '../components/common/CardComponent.vue';
import StatusAlert from '../components/common/StatusAlert.vue';
import WebSocketConsole from '../components/common/WebSocketConsole.vue';
import apiService from '../services/apiService';
import { getWsBaseUrl } from '../utils/dateUtils';

// 상태 관리
const connectionStatus = ref({});
const statusState = reactive({
  status: 'idle',
  message: ''
});

const dbTest = reactive({
  loading: false,
  result: null,
  error: null
});

const redisTest = reactive({
  loading: false,
  key: 'test:key1',
  value: '테스트 값 ' + new Date().toLocaleTimeString(),
  result: null,
  error: null
});

const kafkaTest = reactive({
  loading: false,
  message: '테스트 메시지 ' + new Date().toLocaleTimeString(),
  result: null,
  error: null
});

// 메서드
const fetchConnectionStatus = async () => {
  statusState.status = 'loading';
  statusState.message = '연결 상태 확인 중...';
  
  try {
    const response = await apiService.getConnectionStatus();
    connectionStatus.value = response.data;
    statusState.status = 'success';
    statusState.message = '연결 상태 확인 완료';
  } catch (error) {
    console.error('연결 상태 확인 오류:', error);
    statusState.status = 'error';
    statusState.message = '연결 상태 확인 중 오류 발생: ' + (error.response?.data?.message || error.message);
  }
};

const testDatabase = async () => {
  dbTest.loading = true;
  dbTest.result = null;
  dbTest.error = null;
  
  try {
    const response = await apiService.testDatabaseConnection();
    dbTest.result = response.data;
  } catch (error) {
    console.error('데이터베이스 테스트 오류:', error);
    dbTest.error = error.response?.data?.message || error.message;
  } finally {
    dbTest.loading = false;
  }
};

const testRedis = async () => {
  redisTest.loading = true;
  redisTest.result = null;
  redisTest.error = null;
  
  try {
    const response = await apiService.testRedisConnection({
      key: redisTest.key,
      value: redisTest.value
    });
    redisTest.result = response.data;
  } catch (error) {
    console.error('Redis 테스트 오류:', error);
    redisTest.error = error.response?.data?.message || error.message;
  } finally {
    redisTest.loading = false;
  }
};

const testKafka = async () => {
  kafkaTest.loading = true;
  kafkaTest.result = null;
  kafkaTest.error = null;
  
  try {
    const response = await apiService.testKafkaConnection({
      message: kafkaTest.message
    });
    kafkaTest.result = response.data;
  } catch (error) {
    console.error('Kafka 테스트 오류:', error);
    kafkaTest.error = error.response?.data?.message || error.message;
  } finally {
    kafkaTest.loading = false;
  }
};

// 초기화
onMounted(() => {
  fetchConnectionStatus();
});
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">연결 테스트</h1>
    
    <StatusAlert :status="statusState.status" :message="statusState.message" />
    
    <!-- 연결 상태 -->
    <CardComponent title="시스템 연결 상태" :loading="statusState.status === 'loading'">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
        <div v-for="(status, key) in connectionStatus" :key="key" class="bg-gray-50 p-4 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">{{ key }}</div>
          <div v-if="key === 'timestamp'" class="font-medium">{{ status }}</div>
          <div v-else :class="[
            'font-medium',
            status === 'OK' || status === 'CONNECTED' ? 'text-green-600' : '',
            status === 'NOT_CONFIGURED' ? 'text-yellow-600' : '',
            status === 'ERROR' ? 'text-red-600' : ''
          ]">
            {{ status }}
          </div>
        </div>
      </div>
      
      <div class="flex justify-end">
        <button @click="fetchConnectionStatus" class="btn btn-primary">새로고침</button>
      </div>
    </CardComponent>
    
    <!-- 데이터베이스 테스트 -->
    <CardComponent title="데이터베이스 연결 테스트" :loading="dbTest.loading" class="mt-6">
      <div v-if="dbTest.result" class="mb-4">
        <div :class="[
          'p-4 rounded-lg',
          dbTest.result.status === 'OK' ? 'bg-green-50' : '',
          dbTest.result.status === 'ERROR' ? 'bg-red-50' : '',
          dbTest.result.status === 'NOT_CONFIGURED' ? 'bg-yellow-50' : ''
        ]">
          <div class="font-medium mb-2">{{ dbTest.result.message }}</div>
          <div v-if="dbTest.result.currentTime" class="text-sm">현재 DB 시간: {{ dbTest.result.currentTime }}</div>
          <div v-if="dbTest.result.error" class="text-red-600 text-sm mt-2">{{ dbTest.result.error }}</div>
        </div>
      </div>
      
      <div v-if="dbTest.error" class="mb-4 p-4 bg-red-50 text-red-600 rounded-lg">
        {{ dbTest.error }}
      </div>
      
      <div class="flex justify-end">
        <button @click="testDatabase" class="btn btn-primary" :disabled="dbTest.loading">
          테스트 실행
        </button>
      </div>
    </CardComponent>
    
    <!-- Redis 테스트 -->
    <CardComponent title="Redis 연결 테스트" :loading="redisTest.loading" class="mt-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div class="form-group">
          <label for="redis-key" class="form-label">키</label>
          <input id="redis-key" v-model="redisTest.key" type="text" class="form-input" />
        </div>
        
        <div class="form-group">
          <label for="redis-value" class="form-label">값</label>
          <input id="redis-value" v-model="redisTest.value" type="text" class="form-input" />
        </div>
      </div>
      
      <div v-if="redisTest.result" class="mb-4">
        <div :class="[
          'p-4 rounded-lg',
          redisTest.result.status === 'OK' ? 'bg-green-50' : '',
          redisTest.result.status === 'ERROR' ? 'bg-red-50' : '',
          redisTest.result.status === 'NOT_CONFIGURED' ? 'bg-yellow-50' : ''
        ]">
          <div class="font-medium mb-2">{{ redisTest.result.message }}</div>
          <div v-if="redisTest.result.key && redisTest.result.value" class="text-sm">
            저장된 값: <code class="bg-gray-200 px-2 py-1 rounded">{{ redisTest.result.key }}</code> = <code class="bg-gray-200 px-2 py-1 rounded">{{ redisTest.result.value }}</code>
          </div>
          <div v-if="redisTest.result.error" class="text-red-600 text-sm mt-2">{{ redisTest.result.error }}</div>
        </div>
      </div>
      
      <div v-if="redisTest.error" class="mb-4 p-4 bg-red-50 text-red-600 rounded-lg">
        {{ redisTest.error }}
      </div>
      
      <div class="flex justify-end">
        <button @click="testRedis" class="btn btn-primary" :disabled="redisTest.loading">
          테스트 실행
        </button>
      </div>
    </CardComponent>
    
    <!-- Kafka 테스트 -->
    <CardComponent title="Kafka 연결 테스트" :loading="kafkaTest.loading" class="mt-6">
      <div class="form-group">
        <label for="kafka-message" class="form-label">메시지</label>
        <input id="kafka-message" v-model="kafkaTest.message" type="text" class="form-input" />
      </div>
      
      <div v-if="kafkaTest.result" class="mb-4">
        <div :class="[
          'p-4 rounded-lg',
          kafkaTest.result.status === 'OK' ? 'bg-green-50' : '',
          kafkaTest.result.status === 'ERROR' ? 'bg-red-50' : '',
          kafkaTest.result.status === 'NOT_CONFIGURED' ? 'bg-yellow-50' : ''
        ]">
          <div class="font-medium mb-2">{{ kafkaTest.result.message }}</div>
          <div v-if="kafkaTest.result.sentData" class="text-sm">
            <div class="font-medium mb-1">전송된 데이터:</div>
            <pre class="bg-gray-100 p-2 rounded text-xs overflow-auto">{{ JSON.stringify(kafkaTest.result.sentData, null, 2) }}</pre>
          </div>
          <div v-if="kafkaTest.result.error" class="text-red-600 text-sm mt-2">{{ kafkaTest.result.error }}</div>
        </div>
      </div>
      
      <div v-if="kafkaTest.error" class="mb-4 p-4 bg-red-50 text-red-600 rounded-lg">
        {{ kafkaTest.error }}
      </div>
      
      <div class="flex justify-end">
        <button @click="testKafka" class="btn btn-primary" :disabled="kafkaTest.loading">
          테스트 실행
        </button>
      </div>
    </CardComponent>
    
    <!-- WebSocket 테스트 -->
    <h2 class="text-2xl font-bold mt-8 mb-4">WebSocket 테스트</h2>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <h3 class="text-lg font-semibold mb-2">에코 테스트</h3>
        <p class="text-gray-600 mb-4">메시지를 서버로 보내면 그대로 돌려받습니다.</p>
        <WebSocketConsole :url="`${getWsBaseUrl()}/ws/echo`" />
      </div>
      
      <div>
        <h3 class="text-lg font-semibold mb-2">이벤트 스트림 테스트</h3>
        <p class="text-gray-600 mb-4">서버에서 주기적으로 이벤트를 푸시합니다.</p>
        <WebSocketConsole :url="`${getWsBaseUrl()}/ws/events`" />
      </div>
    </div>
  </div>
</template>
