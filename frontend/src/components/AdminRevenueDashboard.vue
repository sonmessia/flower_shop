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
      <h3>Doanh thu năm {{ selectedYear }} (Nhấp vào cột để xem chi tiết tháng)</h3>
      <div class="chart-container" v-if="chartData.labels && chartData.labels.length > 0">
        <Bar id="revenue-chart" :options="chartOptions" :data="chartData" />
      </div>
      <div v-else-if="!loading" class="empty-chart">
        Chưa có dữ liệu biểu đồ.
      </div>
    </div>

    <!-- Monthly Detail Modal -->
    <transition name="fade">
      <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
        <div class="modal-content large">
          <header class="modal-header">
            <h3>Chi tiết doanh thu Tháng {{ selectedMonth }} / {{ selectedYear }}</h3>
            <button type="button" class="close-btn" @click="closeModal">✕</button>
          </header>

          <div class="modal-body" v-if="monthlyData && !isMonthlyLoading">
            <div class="chart-container monthly-chart">
              <Line :options="monthlyChartOptions" :data="monthlyChartData" />
            </div>

            <div class="orders-section">
              <h4>Danh sách đơn hàng hoàn thành ({{ monthlyData.orders.length }})</h4>
              <div class="table-responsive">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Mã ĐH</th>
                      <th>Ngày tạo</th>
                      <th>Khách hàng</th>
                      <th>Doanh thu</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in monthlyData.orders" :key="order.id">
                      <td>#{{ order.id }}</td>
                      <td>{{ new Date(order.createdAt).toLocaleString('vi-VN') }}</td>
                      <td>{{ order.shippingName }}</td>
                      <td>{{ formatCurrency(order.totalAmount) }}</td>
                    </tr>
                    <tr v-if="!monthlyData.orders.length">
                      <td colspan="4" class="empty">Không có đơn hàng nào trong tháng này.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <div class="modal-body loading" v-else>
            <div class="spinner"></div>
            <p>Đang tải dữ liệu chi tiết...</p>
          </div>
        </div>
      </div>
    </transition>
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
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
} from 'chart.js';
import { Bar, Line } from 'vue-chartjs';

ChartJS.register(Title, Tooltip, Legend, BarElement, LineElement, PointElement, CategoryScale, LinearScale);

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

const isModalOpen = ref(false);
const monthlyData = ref(null);
const selectedMonth = ref(null);
const isMonthlyLoading = ref(false);

const monthlyChartData = ref({
  labels: [],
  datasets: [{ data: [] }]
});

const monthlyChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
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

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  onClick: (event, elements) => {
    if (elements.length > 0) {
      const index = elements[0].index;
      openMonthlyDetail(index + 1);
    }
  },
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

const openMonthlyDetail = async (month) => {
  selectedMonth.value = month;
  isModalOpen.value = true;
  isMonthlyLoading.value = true;
  monthlyData.value = null;

  try {
    const res = await axios.get(`/admins/dashboard/monthly-detail?year=${selectedYear.value}&month=${month}`);
    monthlyData.value = res.data;

    monthlyChartData.value = {
      labels: res.data.labels,
      datasets: [
        {
          label: 'Doanh thu ngày (VND)',
          backgroundColor: 'rgba(236, 72, 153, 0.2)', // Pink transparent
          borderColor: '#EC4899', // Pink solid
          borderWidth: 2,
          pointBackgroundColor: '#EC4899',
          tension: 0.3,
          fill: true,
          data: res.data.data
        }
      ]
    };
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu chi tiết tháng', error);
    alert('Không thể tải chi tiết tháng này.');
  } finally {
    isMonthlyLoading.value = false;
  }
};

const closeModal = () => {
  isModalOpen.value = false;
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

/* Modal Styles */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(17, 24, 39, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(4px);
}

.modal-content.large {
  background: white;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #111827;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6B7280;
  cursor: pointer;
}

.close-btn:hover {
  color: #EF4444;
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.modal-body.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #6B7280;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #8B5CF6;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.monthly-chart {
  height: 250px;
  margin-bottom: 2rem;
}

.orders-section h4 {
  margin-bottom: 1rem;
  color: #374151;
}

.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #E5E7EB;
}

.data-table th {
  background: #F9FAFB;
  font-weight: 600;
  color: #374151;
}

.data-table td {
  color: #4B5563;
}

.data-table tr:hover td {
  background: #F9FAFB;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
