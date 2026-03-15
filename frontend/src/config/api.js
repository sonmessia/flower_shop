// API Configuration
const API_BASE_URL =
	process.env.VUE_APP_API_BASE || "http://localhost:8081/api";

export default {
	baseURL: API_BASE_URL,

	// Endpoints
	products: {
		getAll: () => `${API_BASE_URL}/products`,
		getById: (id) => `${API_BASE_URL}/products/${id}`,
		create: () => `${API_BASE_URL}/products`,
		update: (id) => `${API_BASE_URL}/products/${id}`,
		delete: (id) => `${API_BASE_URL}/products/${id}`,

		// Image upload endpoints
		uploadMainImage: (id) => `${API_BASE_URL}/products/${id}/images/main`,
		uploadMainImageUrl: (id) =>
			`${API_BASE_URL}/products/${id}/images/main-url`,
		uploadImage: (id) => `${API_BASE_URL}/products/${id}/images`,
		uploadImageUrl: (id) => `${API_BASE_URL}/products/${id}/images/url`,

		// Image retrieval endpoints
		getMainImage: (id) => `${API_BASE_URL}/products/${id}/image`,
		getImage: (productId, imageId) =>
			`${API_BASE_URL}/products/${productId}/images/${imageId}`,

		// Image delete endpoints
		deleteMainImage: (id) => `${API_BASE_URL}/products/${id}/images/main`,
		deleteImage: (productId, imageId) =>
			`${API_BASE_URL}/products/${productId}/images/${imageId}`,
		deleteAllImages: (id) => `${API_BASE_URL}/products/${id}/images`,

		// Legacy endpoint (kept for backward compatibility)
		uploadProductImage: (id) => `${API_BASE_URL}/products/${id}/images`,
	},

	categories: {
		getAll: () => `${API_BASE_URL}/categories`,
		getById: (id) => `${API_BASE_URL}/categories/${id}`,
		getProducts: (id) => `${API_BASE_URL}/categories/${id}/products`,
		create: () => `${API_BASE_URL}/categories`,
		update: (id) => `${API_BASE_URL}/categories/${id}`,
		delete: (id) => `${API_BASE_URL}/categories/${id}`,
	},

	collections: {
		getAll: () => `${API_BASE_URL}/collections`,
		getById: (id) => `${API_BASE_URL}/collections/${id}`,
		getProducts: (id) => `${API_BASE_URL}/collections/${id}/products`,
		create: () => `${API_BASE_URL}/collections`,
		update: (id) => `${API_BASE_URL}/collections/${id}`,
		delete: (id) => `${API_BASE_URL}/collections/${id}`,
	},

	admin: {
		login: () => `${API_BASE_URL}/admins/login`,
	},

	users: {
		register: () => `${API_BASE_URL}/users/register`,
		login: () => `${API_BASE_URL}/users/login`,
		refresh: () => `${API_BASE_URL}/users/refresh`,
		me: () => `${API_BASE_URL}/users/me`,
		updateMe: () => `${API_BASE_URL}/users/me`,
	},

	cart: {
		get: () => `${API_BASE_URL}/cart`,
		addItem: () => `${API_BASE_URL}/cart/items`,
		updateItem: (cartItemId) => `${API_BASE_URL}/cart/items/${cartItemId}`,
		removeItem: (cartItemId) => `${API_BASE_URL}/cart/items/${cartItemId}`,
		clear: () => `${API_BASE_URL}/cart/clear`,
	},

	orders: {
		checkout: () => `${API_BASE_URL}/orders/checkout`,
		mine: () => `${API_BASE_URL}/orders/me`,
		myById: (orderId) => `${API_BASE_URL}/orders/me/${orderId}`,
		cancelMine: (orderId) => `${API_BASE_URL}/orders/me/${orderId}/cancel`,
	},

	adminOrders: {
		getAll: () => `${API_BASE_URL}/admins/orders`,
		updateStatus: (orderId) => `${API_BASE_URL}/admins/orders/${orderId}/status`,
	},

	blogs: {
		getAll: () => `${API_BASE_URL}/blogs`,
		getById: (id) => `${API_BASE_URL}/blogs/${id}`,
		search: (keyword) =>
			`${API_BASE_URL}/blogs?search=${encodeURIComponent(keyword)}`,

		// Admin endpoints
		getAllAdmin: () => `${API_BASE_URL}/admins/blogs`,
		create: () => `${API_BASE_URL}/admins/blogs`,
		update: (id) => `${API_BASE_URL}/admins/blogs/${id}`,
		delete: (id) => `${API_BASE_URL}/admins/blogs/${id}`,
		publish: (id) => `${API_BASE_URL}/admins/blogs/${id}/publish`,
		unpublish: (id) => `${API_BASE_URL}/admins/blogs/${id}/unpublish`,
		getByAuthor: (authorId) =>
			`${API_BASE_URL}/admins/blogs/author/${authorId}`,

		// Image upload endpoints
		uploadMainImage: (id) => `${API_BASE_URL}/admins/blogs/${id}/images/main`,
		uploadMainImageUrl: (id) =>
			`${API_BASE_URL}/admins/blogs/${id}/images/main-url`,
		uploadImage: (id) => `${API_BASE_URL}/admins/blogs/${id}/images`,
		uploadImageUrl: (id) => `${API_BASE_URL}/admins/blogs/${id}/images/url`,
		deleteMainImage: (id) => `${API_BASE_URL}/admins/blogs/${id}/images/main`,
		deleteImage: (blogId, imageId) =>
			`${API_BASE_URL}/admins/blogs/${blogId}/images/${imageId}`,
	},

	chat: {
		history: (roomId) => `${API_BASE_URL}/chat/history/${roomId}`,
		wsUrl: () => API_BASE_URL.replace(/\/api\/?$/, '/ws'),
		adminRooms: () => `${API_BASE_URL}/chat/rooms`,
	},
};
