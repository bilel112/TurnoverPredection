import axios from 'axios';

const API_BASE_URL = 'http://localhost:8083/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const savedToken = window.localStorage.getItem('accessToken');
if (savedToken) {
  apiClient.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`;
}

const ensureAuth = async () => {
  const existingToken = window.localStorage.getItem('accessToken');
  if (existingToken) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${existingToken}`;
    return existingToken;
  }

  try {
    const response = await apiClient.post('/auth/login', { username: 'rh1', password: 'rh123' });
    const headerToken = response.headers?.authorization || response.headers?.Authorization;
    const token = headerToken ? headerToken.replace(/^Bearer\s+/i, '') : response.data?.token;
    if (token) {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      window.localStorage.setItem('accessToken', token);
      return token;
    }
  } catch (e) {
    console.error('Auto-login failed', e);
  }

  return null;
};

apiClient.interceptors.request.use(async (config) => {
  if (config.url?.includes('/auth/login')) {
    return config;
  }

  const token = window.localStorage.getItem('accessToken');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
    return config;
  }

  const freshToken = await ensureAuth();
  if (freshToken) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${freshToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      delete apiClient.defaults.headers.common['Authorization'];
      window.localStorage.removeItem('accessToken');
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

export const EmployeeService = {
  getAll: async () => {
    const response = await apiClient.get('/employees');
    return response.data;
  },
  getPaginated: async (page = 0, size = 10, sort = '') => {
    let url = `/employees/paginated?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    const response = await apiClient.get(url);
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/employees/${id}`);
    return response.data;
  },
  create: async (employee) => {
    const response = await apiClient.post('/employees', employee);
    return response.data;
  },
  update: async (id, employee) => {
    const response = await apiClient.put(`/employees/${id}`, employee);
    return response.data;
  },
  delete: async (id) => {
    await apiClient.delete(`/employees/${id}`);
  },
  getCount: async () => {
    const response = await apiClient.get('/employees/count');
    return response.data;
  },
};

export const AuthService = {
  login: async (username, password) => {
    const response = await apiClient.post('/auth/login', { username, password });
    // Prefer token from Authorization header (added by backend), fallback to body.token
    const headerToken = response.headers?.authorization || response.headers?.Authorization;
    const token = headerToken ? headerToken.replace(/^Bearer\s+/i, '') : response.data?.token;
    if (token) {
      AuthService.setToken(token);
    }
    return response.data;
  },
  me: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },
  setToken: (token) => {
    if (token) {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      window.localStorage.setItem('accessToken', token);
    } else {
      delete apiClient.defaults.headers.common['Authorization'];
      window.localStorage.removeItem('accessToken');
    }
  },
  clearToken: () => {
    delete apiClient.defaults.headers.common['Authorization'];
    window.localStorage.removeItem('accessToken');
  },
};

export const UserService = {
  getAll: async () => {
    const response = await apiClient.get('/users');
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/users/${id}`);
    return response.data;
  },
  getByUsername: async (username) => {
    const response = await apiClient.get(`/users/username/${encodeURIComponent(username)}`);
    return response.data;
  },
  create: async (user) => {
    const response = await apiClient.post('/users', user);
    return response.data;
  },
  update: async (id, user) => {
    const response = await apiClient.put(`/users/${id}`, user);
    return response.data;
  },
  delete: async (id) => {
    await apiClient.delete(`/users/${id}`);
  },
};

export const RoleService = {
  getAll: async () => {
    const response = await apiClient.get('/roles');
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/roles/${id}`);
    return response.data;
  },
  create: async (role) => {
    const response = await apiClient.post('/roles', role);
    return response.data;
  },
  delete: async (id) => {
    await apiClient.delete(`/roles/${id}`);
  },
};

export const DynamicTurnoverService = {
  getScoreForEmployee: async (employeeId) => {
    const response = await apiClient.get(`/turnover-scoring/employees/${employeeId}/score`);
    return response.data;
  },
  getScoreHistoryForEmployee: async (employeeId) => {
    const response = await apiClient.get(`/turnover-scoring/employees/${employeeId}/history`);
    return response.data;
  },
};

export const RecommendationService = {
  generateForEmployee: async (employeeId) => {
    const response = await apiClient.post(`/recommendations/employees/${employeeId}`);
    return response.data;
  },
};

export const AlertService = {
  list: async (status, severity) => {
    let url = '/alerts';
    const params = [];
    if (status) params.push(`status=${encodeURIComponent(status)}`);
    if (severity) params.push(`severity=${encodeURIComponent(severity)}`);
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    const response = await apiClient.get(url);
    return response.data;
  },
  summary: async () => {
    const response = await apiClient.get('/alerts/summary');
    return response.data;
  },
  createForEmployee: async (employeeId, payload) => {
    const response = await apiClient.post(`/alerts/employees/${employeeId}`, payload);
    return response.data;
  },
  markRead: async (alertId) => {
    const response = await apiClient.patch(`/alerts/${alertId}/read`);
    return response.data;
  },
  resolve: async (alertId) => {
    const response = await apiClient.patch(`/alerts/${alertId}/resolve`);
    return response.data;
  },
  getConfig: async () => {
    const response = await apiClient.get('/alerts/config');
    return response.data;
  },
  updateConfig: async (payload) => {
    const response = await apiClient.put('/alerts/config', payload);
    return response.data;
  },
  sendHighRiskReport: async () => {
    const response = await apiClient.post('/alerts/report');
    return response.data;
  }
};

export default apiClient;
