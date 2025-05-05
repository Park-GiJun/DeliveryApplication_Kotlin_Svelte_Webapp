import axios from 'axios';
import { getApiBaseUrl } from '../utils/dateUtils';

const apiClient = axios.create({
  baseURL: getApiBaseUrl(),
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000
});

export default {
  /**
   * 시스템 연결 상태 가져오기
   */
  getConnectionStatus() {
    return apiClient.get('/api/connection-test/status');
  },
  
  /**
   * 데이터베이스 연결 테스트
   */
  testDatabaseConnection() {
    return apiClient.get('/api/connection-test/database');
  },
  
  /**
   * Redis 연결 테스트
   */
  testRedisConnection(data = {}) {
    return apiClient.post('/api/connection-test/redis', data);
  },
  
  /**
   * Kafka 연결 테스트
   */
  testKafkaConnection(data = {}) {
    return apiClient.post('/api/connection-test/kafka', data);
  },
  
  /**
   * 스트림 데이터 받기 (SSE 대신 일반 스트림으로 구현)
   */
  getStreamData(count = 5, callback) {
    // 스트림 형태로 데이터를 핸들링하기 위한 특별한 처리
    return apiClient.get(`/api/connection-test/stream?count=${count}`, {
      responseType: 'stream'
    }).then(response => {
      // 여기서는 실제 스트림 처리를 못하므로 일반 응답으로 처리
      if (callback && typeof callback === 'function') {
        callback(response.data);
      }
      return response.data;
    });
  }
};
