<template>
  <AdminLayout v-if="$route.path.startsWith('/admin')" />
  <Layout v-else />
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import AdminLayout from '@/components/AdminLayout.vue'
</script>

<style>
body { margin: 0; padding: 0; background-color: #f5f5f5; }
</style>
