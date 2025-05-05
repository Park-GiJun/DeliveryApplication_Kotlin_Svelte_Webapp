<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { formatDate } from '../../utils/dateUtils';

const props = defineProps({
  url: {
    type: String,
    required: true
  }
});

const socket = ref(null);
const connected = ref(false);
const messages = ref([]);
const inputMessage = ref('');
const error = ref('');

const connect = () => {
  try {
    socket.value = new WebSocket(props.url);
    
    socket.value.onopen = () => {
      connected.value = true;
      error.value = '';
      addMessage('시스템', '웹소켓 연결이 설정되었습니다', 'success');
    };
    
    socket.value.onmessage = (event) => {
      try {
        // JSON으로 파싱 시도
        const data = JSON.parse(event.data);
        addMessage('서버', data, 'received');
      } catch (e) {
        // 일반 텍스트로 처리
        addMessage('서버', event.data, 'received');
      }
    };
    
    socket.value.onclose = () => {
      connected.value = false;
      addMessage('시스템', '웹소켓 연결이 종료되었습니다', 'info');
    };
    
    socket.value.onerror = (err) => {
      error.value = '연결 오류가 발생했습니다';
      addMessage('시스템', '웹소켓 오류: ' + JSON.stringify(err), 'error');
    };
  } catch (err) {
    error.value = err.message;
  }
};

const disconnect = () => {
  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.close();
  }
};

const sendMessage = () => {
  if (socket.value && socket.value.readyState === WebSocket.OPEN && inputMessage.value) {
    socket.value.send(inputMessage.value);
    addMessage('클라이언트', inputMessage.value, 'sent');
    inputMessage.value = '';
  }
};

const addMessage = (from, content, type) => {
  const message = {
    id: Date.now(),
    from,
    content,
    type,
    timestamp: new Date()
  };
  messages.value.push(message);
  
  // 메시지가 너무 많아지면 오래된 것부터 삭제
  if (messages.value.length > 50) {
    messages.value.shift();
  }
};

const clearMessages = () => {
  messages.value = [];
};

onMounted(() => {
  connect();
});

onUnmounted(() => {
  disconnect();
});
</script>

<template>
  <div class="websocket-console border border-gray-300 rounded-lg overflow-hidden">
    <div class="bg-gray-100 p-3 border-b flex justify-between items-center">
      <div class="font-medium">WebSocket 콘솔: {{ url }}</div>
      <div class="flex items-center space-x-2">
        <span :class="[
          'inline-block w-3 h-3 rounded-full',
          connected ? 'bg-green-500' : 'bg-red-500'
        ]"></span>
        <span>{{ connected ? '연결됨' : '연결 안됨' }}</span>
        <button 
          v-if="!connected" 
          @click="connect"
          class="bg-blue-500 hover:bg-blue-600 text-white px-2 py-1 rounded text-sm"
        >
          연결
        </button>
        <button 
          v-else 
          @click="disconnect"
          class="bg-gray-500 hover:bg-gray-600 text-white px-2 py-1 rounded text-sm"
        >
          연결 해제
        </button>
        <button 
          @click="clearMessages"
          class="bg-gray-500 hover:bg-gray-600 text-white px-2 py-1 rounded text-sm"
        >
          지우기
        </button>
      </div>
    </div>
    
    <div class="messages-container bg-white h-64 overflow-y-auto p-3">
      <div v-if="error" class="mb-3 text-red-500">{{ error }}</div>
      
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="[
          'mb-2 p-2 rounded max-w-3xl',
          message.type === 'sent' ? 'bg-blue-50 ml-auto' : '',
          message.type === 'received' ? 'bg-gray-50' : '',
          message.type === 'success' ? 'bg-green-50 mx-auto text-center' : '',
          message.type === 'error' ? 'bg-red-50 mx-auto text-center' : '',
          message.type === 'info' ? 'bg-yellow-50 mx-auto text-center' : ''
        ]"
      >
        <div class="text-xs text-gray-500 flex justify-between">
          <span>{{ message.from }}</span>
          <span>{{ formatDate(message.timestamp) }}</span>
        </div>
        <div class="mt-1">
          <pre v-if="typeof message.content === 'object'" class="text-sm whitespace-pre-wrap">{{ JSON.stringify(message.content, null, 2) }}</pre>
          <span v-else class="text-sm">{{ message.content }}</span>
        </div>
      </div>
      
      <div v-if="messages.length === 0" class="text-center text-gray-500 py-4">
        메시지 없음
      </div>
    </div>
    
    <div class="input-container p-3 border-t">
      <div class="flex">
        <input 
          v-model="inputMessage"
          type="text" 
          placeholder="메시지 입력..."
          class="flex-grow border border-gray-300 rounded-l p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          @keyup.enter="sendMessage"
        />
        <button 
          @click="sendMessage"
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-r"
          :disabled="!connected"
        >
          전송
        </button>
      </div>
    </div>
  </div>
</template>
