<template>
  <AdminLayout v-if="$route.path.startsWith('/admin')" />
  <Layout v-else />
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import AdminLayout from '@/components/AdminLayout.vue'
</script>
