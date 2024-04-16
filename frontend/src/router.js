import {createRouter, createWebHashHistory, createWebHistory,} from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('@/pages/Login/Login.vue')
        },
        {
            path: '/home',
            name: 'home',
            redirect: '/home/item',
            component: () => import('@/pages/Home/Home.vue'),
            children: [
                {
                    path: '/home/item',
                    name: 'item',
                    component: () => import('@/pages/Item/NavList.vue'),
                },
                {
                    path: '/home/item/year',
                    name: 'itemyear',
                    component: () => import('@/pages/Item/Item.vue'),
                },
                {
                    path: '/home/lab',
                    name: 'lab',
                    component: () => import('@/pages/Lab/Lab.vue'),
                },
                {
                    path: '/home/lab/detail',
                    name: 'labDetail',
                    component: () => import('@/pages/LabDetail/LabDetail.vue'),
                },
                {
                    path: '/home/user',
                    name: 'user',
                    component: () => import('@/pages/User/User.vue'),
                },
                {
                    path: '/home/manager',
                    name: 'manager',
                    component: () => import('@/pages/Manager/Manager.vue'),
                },
            ]
        },
        {
            path: '/:pathMatch(.*)*',
            name: '404',
            meta: {status: 404},
            component: () => import('@/pages/404.vue'),
        }
    ]
})

export default router
