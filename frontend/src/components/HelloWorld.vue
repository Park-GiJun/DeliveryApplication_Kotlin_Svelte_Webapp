<script setup>
defineProps({
  msg: {
    type: String,
    required: true
  }
})
</script>

<template>
  <div class="greetings">
    <h1 class="green">{{ msg }}</h1>
    <h3>
      배달 시뮬레이터에 오신 것을 환영합니다.
    </h3>
  </div>
</template>

<style scoped>
h1 {
  font-weight: 700;
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

h3 {
  font-size: 1rem;
  color: #666;
}

.green {
  color: #4caf50;
}
</style>
