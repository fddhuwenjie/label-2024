<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const locale = zhCn
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f7fa;
}

/* 全局表格样式 - 统一表头和内容对齐 */
.el-table th.el-table__cell,
.el-table td.el-table__cell {
  padding: 12px 0;
}
.el-table th.el-table__cell .cell,
.el-table td.el-table__cell .cell {
  padding-left: 16px;
  padding-right: 16px;
}
</style>
