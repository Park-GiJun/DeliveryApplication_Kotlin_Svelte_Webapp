<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="welcome-container">
    <h1 class="text-3xl font-bold mb-6">배달 시뮬레이션 시스템</h1>
    <p class="mb-6">이 애플리케이션은 실시간 배달 매칭 시스템을 시뮬레이션합니다.</p>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="welcome-card">
        <h2 class="text-xl font-bold mb-3">고객 앱</h2>
        <p>주문을 생성하고 배달 상태를 모니터링합니다.</p>
        <RouterLink to="/customer" class="btn mt-4 w-full">
          고객 앱으로 이동
        </RouterLink>
      </div>
      
      <div class="welcome-card">
        <h2 class="text-xl font-bold mb-3">라이더 앱</h2>
        <p>배달 주문을 수락하고 배달 과정을 관리합니다.</p>
        <RouterLink to="/rider" class="btn mt-4 w-full">
          라이더 앱으로 이동
        </RouterLink>
      </div>
      
      <div class="welcome-card">
        <h2 class="text-xl font-bold mb-3">매장 앱</h2>
        <p>주문을 접수하고 상품과 매장을 관리합니다.</p>
        <RouterLink to="/store" class="btn mt-4 w-full">
          매장 앱으로 이동
        </RouterLink>
      </div>
    </div>
    
    <div class="mt-10">
      <h2 class="text-2xl font-bold mb-3">테스트 및 설정</h2>
      <p class="mb-4">시스템 연결 및 컴포넌트 테스트를 위한 페이지입니다.</p>
      <RouterLink to="/test" class="btn btn-secondary">
        테스트 페이지로 이동
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.welcome-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.welcome-card {
  background-color: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.welcome-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.btn {
  display: inline-block;
  background-color: #3b82f6;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  font-weight: 500;
  text-align: center;
  transition: background-color 0.2s;
}

.btn:hover {
  background-color: #2563eb;
}

.btn-secondary {
  background-color: #6b7280;
}

.btn-secondary:hover {
  background-color: #4b5563;
}
</style>
