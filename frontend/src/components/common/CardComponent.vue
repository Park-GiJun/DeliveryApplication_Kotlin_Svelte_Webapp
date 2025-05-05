<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div class="card">
    <div class="flex justify-between items-center mb-4">
      <h2 class="card-title">{{ title }}</h2>
      <div v-if="loading" class="animate-spin h-5 w-5 border-2 border-blue-500 border-t-transparent rounded-full"></div>
    </div>
    <slot></slot>
  </div>
</template>
