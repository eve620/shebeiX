//单例模式
import axios from 'axios';

const $axios = axios.create({
    // baseURL: '/api', // 替换为你的 API 地址
    baseURL: 'http://localhost:9000',
    withCredentials: true
});

let instance;

export default function getInstance() {
    if (!instance) {
        instance = new Instance()
    }
    return instance;
}

class Instance {
    constructor() {
        this.user = null;
    }

    getUser() {
        return this.user;
    }

    setUser(user) {
        this.user = user;
    }

    //Auth API
    whoami() {
        return $axios({
            'url': '/user/whoami',
            'method': 'get',
        })
    }

    loginApi(data) {
        return $axios({
            'url': '/user/login',
            'method': 'post',
            data
        })
    }

    logoutApi() {
        return $axios({
            'url': '/user/logout',
            'method': 'post',
        })

    }

    //Item API
    getItemList(input) {
        return $axios({
            url: '/item/page',
            method: 'get',
            params: {
                input
            }
        })
    }

    //Lab API
    getLabList(input) {
        return $axios({
            url: '/lab/page',
            method: 'get',
            params: {
                input
            }
        })
    }

    //User API
    getUserList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin:false
            }
        })
    }

    //Manager API
    getManagerList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin:true
            }
        })
    }
}