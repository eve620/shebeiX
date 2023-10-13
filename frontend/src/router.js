import {createRouter, createWebHashHistory, createWebHistory,} from 'vue-router'

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            redirect:'/login'
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('@/pages/Login/Login.vue')
        },
        {
            path: '/home',
            name: 'home',
            redirect:'/home/item',
            component: () => import('@/pages/Home/Home.vue'),
            children:[
                {
                    path: '/home/item',
                    name: 'item',
                    component: () => import('@/components/Item/Item.vue'),
                },
                {
                    path: '/home/lab',
                    name: 'lab',
                    component: () => import('@/components/Lab/Lab.vue'),
                },
                {
                    path: '/home/user',
                    name: 'user',
                    component: () => import('@/components/User/User.vue'),
                },
                {
                    path: '/home/manager',
                    name: 'manager',
                    component: () => import('@/components/Manager/Manager.vue'),
                },
            ]
        },
        {
            path: '/:pathMatch(.*)*',
            name: '404',
            meta:{status:404},
            component: () => import('@/pages/404.vue'),
        }
    ]
})

export default router
