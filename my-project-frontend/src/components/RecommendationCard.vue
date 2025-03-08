<script setup>

</script>

<template>
    <div class="recommendation-card">
        <slot/>
    </div>
</template>

<style scoped>
.recommendation-card {
    background-color: var(--el-bg-color);
    border-radius: 5px;
    padding: 10px;
}
</style>