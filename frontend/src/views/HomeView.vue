<script setup>
// HomeView.vue - 메인 대시보드
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-6">배달 시뮬레이터 대시보드</h1>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="card">
        <h2 class="card-title">시스템 정보</h2>
        <div class="space-y-2">
          <p><strong>환경:</strong> 개발</p>
          <p><strong>버전:</strong> 0.1.0</p>
          <p><strong>상태:</strong> <span class="text-green-500 font-semibold">활성</span></p>
        </div>
      </div>
      
      <div class="card">
        <h2 class="card-title">시뮬레이션 현황</h2>
        <div class="flex justify-between">
          <div class="text-center">
            <span class="block text-2xl font-bold text-blue-500">0</span>
            <span class="text-sm text-gray-500">활성 주문</span>
          </div>
          <div class="text-center">
            <span class="block text-2xl font-bold text-green-500">0</span>
            <span class="text-sm text-gray-500">활성 라이더</span>
          </div>
          <div class="text-center">
            <span class="block text-2xl font-bold text-purple-500">0</span>
            <span class="text-sm text-gray-500">완료 주문</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="mt-8">
      <h2 class="text-2xl font-bold mb-4">빠른 시작</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-white rounded-lg shadow-md p-6 border-t-4 border-blue-500">
          <h3 class="font-bold mb-2">연결 테스트</h3>
          <p class="text-gray-600 mb-4">데이터베이스, Redis, Kafka, WebSocket 연결을 테스트합니다.</p>
          <router-link to="/test" class="btn btn-primary block text-center">테스트 시작</router-link>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6 border-t-4 border-green-500">
          <h3 class="font-bold mb-2">배달 앱 시뮬레이션</h3>
          <p class="text-gray-600 mb-4">고객, 라이더, 매장용 앱 화면을 시뮬레이션합니다.</p>
          <div class="grid grid-cols-3 gap-2">
            <router-link to="/customer" class="btn btn-secondary block text-center text-xs py-1">고객앱</router-link>
            <router-link to="/rider" class="btn btn-secondary block text-center text-xs py-1">라이더앱</router-link>
            <router-link to="/store" class="btn btn-secondary block text-center text-xs py-1">매장앱</router-link>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6 border-t-4 border-yellow-500">
          <h3 class="font-bold mb-2">시뮬레이션 시작</h3>
          <p class="text-gray-600 mb-4">자동화된 배달 프로세스 시뮬레이션을 시작합니다.</p>
          <button disabled class="btn bg-gray-300 text-gray-600 cursor-not-allowed block w-full text-center">준비 중</button>
        </div>
      </div>
    </div>
  </div>
</template>
