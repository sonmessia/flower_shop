import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../components/HomePage.vue";
import ProductDetail from "../components/ProductDetail.vue";
import AdminLogin from "../components/AdminLogin.vue";
import AdminDashboard from "../components/AdminDashboard.vue";
import BlogList from "../components/BlogList.vue";
import BlogDetail from "../components/BlogDetail.vue";
import AdminProductManagement from "../components/AdminProductManagement.vue";
import UserLogin from "../components/UserLogin.vue";
import UserRegister from "../components/UserRegister.vue";
import UserManagement from "../components/UserManagement.vue";

const routes = [
	{
		path: "/",
		name: "Home",
		component: HomePage,
	},
	{
		path: "/products/:id",
		name: "ProductDetail",
		component: ProductDetail,
	},
	{
		path: "/blogs",
		name: "BlogList",
		component: BlogList,
	},
	{
		path: "/blogs/:id",
		name: "BlogDetail",
		component: BlogDetail,
	},
	{
		path: "/login",
		name: "UserLogin",
		component: UserLogin,
	},
	{
		path: "/register",
		name: "UserRegister",
		component: UserRegister,
	},
	{
		path: "/account",
		name: "UserManagement",
		component: UserManagement,
		meta: { requiresUserAuth: true },
	},
	{
		path: "/admin/login",
		name: "AdminLogin",
		component: AdminLogin,
	},
	{
		path: "/admin/dashboard",
		name: "AdminDashboard",
		component: AdminDashboard,
		meta: { requiresAuth: true },
	},
	{
		path: "/admin/products",
		name: "AdminProductManagement",
		component: AdminProductManagement,
		meta: { requiresAuth: true },
	},
];

const router = createRouter({
	history: createWebHistory(process.env.BASE_URL),
	routes,
});

// Navigation guard
router.beforeEach((to, from, next) => {
	const admin = JSON.parse(localStorage.getItem("admin"));
	const user = JSON.parse(localStorage.getItem("user"));

	if (to.meta.requiresAuth && !admin) {
		return next("/admin/login");
	}
	if (to.meta.requiresUserAuth && !user) {
		return next("/login");
	}
	next();
});

export default router;
