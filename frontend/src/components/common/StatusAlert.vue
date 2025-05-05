<script setup>
defineProps({
  status: {
    type: String,
    default: 'idle'  // idle, loading, success, error
  },
  message: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div v-if="status !== 'idle'" 
       :class="[
         'p-4 mb-4 rounded-md',
         status === 'loading' ? 'bg-blue-50 text-blue-700' : '',
         status === 'success' ? 'bg-green-50 text-green-700' : '',
         status === 'error' ? 'bg-red-50 text-red-700' : ''
       ]">
    <div class="flex items-center">
      <div v-if="status === 'loading'" class="mr-2">
        <div class="animate-spin h-4 w-4 border-2 border-current border-t-transparent rounded-full"></div>
      </div>
      <div v-else-if="status === 'success'" class="mr-2">
        <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
        </svg>
      </div>
      <div v-else-if="status === 'error'" class="mr-2">
        <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
        </svg>
      </div>
      <div>{{ message }}</div>
    </div>
  </div>
</template>
