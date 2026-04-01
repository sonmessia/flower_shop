<template>
  <div class="dashboard-wrapper">
    <div class="panel-title">
      <div>
        <h2>Tổng quan hệ thống</h2>
        <p>Thống kê số liệu hệ thống và biểu đồ doanh thu theo tháng của năm.</p>
      </div>
      <div>
        <label>
          Năm:
          <select v-model="selectedYear" @change="fetchData" class="year-select">
            <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
          </select>
        </label>
        <button type="button" class="refresh ghost" @click="fetchData" :disabled="loading">
          {{ loading ? "Đang tải..." : "Làm mới" }}
        </button>
      </div>
    </div>

    <!-- Feedback messages -->
    <div v-if="errorMsg" class="feedback error">
      <strong>Có lỗi:</strong> {{ errorMsg }}
    </div>

    <!-- Summary Cards -->
    <div class="grid four summary-cards" v-if="summary">
      <div class="card stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <small>Doanh thu tháng này</small>
          <strong>{{ formatCurrency(summary.currentMonthRevenue) }}</strong>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <small>Đơn hàng tháng này</small>
          <strong>{{ summary.totalOrdersThisMonth }}</strong>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon">🌷</div>
        <div class="stat-content">
          <small>Tổng sản phẩm</small>
          <strong>{{ summary.totalProducts }}</strong>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <small>Khách hàng</small>
          <strong>{{ summary.totalUsers }}</strong>
        </div>
      </div>
    </div>

    <!-- Revenue Chart -->
    <div class="card chart-card">
      <h3>Doanh thu năm {{ selectedYear }}</h3>
      <div class="chart-container" v-if="chartData.labels && chartData.labels.length > 0">
        <Bar id="revenue-chart" :options="chartOptions" :data="chartData" />
      </div>
      <div v-else-if="!loading" class="empty-chart">
        Chưa có dữ liệu biểu đồ.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from '../config/axiosConfig';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

const summary = ref(null);
const loading = ref(false);
const errorMsg = ref('');
const selectedYear = ref(new Date().getFullYear());
const availableYears = ref(
  Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i)
);

const chartData = ref({
  labels: [],
  datasets: [{ data: [] }]
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: (context) => formatCurrency(context.raw)
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (value) => formatCurrency(value)
      }
    }
  }
};

const formatCurrency = (value) => {
  if (value === undefined || value === null) return '0 ₫';
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const fetchData = async () => {
  loading.value = true;
  errorMsg.value = '';
  try {
    const [summaryRes, chartRes] = await Promise.all([
      axios.get('/admins/dashboard/summary'),
      axios.get(`/admins/dashboard/revenue-chart?year=${selectedYear.value}`)
    ]);

    summary.value = summaryRes.data;

    chartData.value = {
      labels: chartRes.data.labels,
      datasets: [
        {
          label: 'Doanh thu (VND)',
          backgroundColor: '#A78BFA',
          borderColor: '#8B5CF6',
          borderWidth: 1,
          hoverBackgroundColor: '#8B5CF6',
          borderRadius: 4,
          data: chartRes.data.data
        }
      ]
    };
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu dashboard', error);
    errorMsg.value = 'Không thể tải dữ liệu thống kê.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.dashboard-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.year-select {
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ddd;
  margin-left: 0.5rem;
  margin-right: 1rem;
}

.grid.four {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
}

.stat-icon {
  font-size: 2.5rem;
  background: #F3F4F6;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-content small {
  color: #6B7280;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.stat-content strong {
  font-size: 1.25rem;
  color: #111827;
}

.chart-card {
  padding: 1.5rem;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-card h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.125rem;
  color: #374151;
}

.chart-container {
  flex: 1;
  position: relative;
  width: 100%;
}

.empty-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9CA3AF;
  font-style: italic;
}

@media (max-width: 1024px) {
  .grid.four {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .grid.four {
    grid-template-columns: 1fr;
  }
}
</style>
