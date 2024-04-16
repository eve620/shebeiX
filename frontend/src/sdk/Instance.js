//单例模式
import axios from 'axios';

const $axios = axios.create({
    // baseURL: '/api', // 替换为你的 API 地址
    // baseURL: '/',
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
    //Common API
    exportExcel(path, data) {
        return $axios({
            url: path + `/download`,
            method: 'post',
            responseType: 'blob',
            data
        })
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
    getItemList(input,labName) {
        return $axios({
            url: '/item/page',
            method: 'get',
            params: {
                input,
                labName
            }
        })
    }

    deleteItemById(id) {
        return $axios({
            url: `/item/${id}`,
            method: 'delete'
        })
    }

    addItem(params) {
        return $axios({
            url: '/item',
            method: 'post',
            data: {...params}
        })
    }

    editItem(params) {
        return $axios({
            url: '/item',
            method: 'put',
            data: {...params}
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

    deleteLabById(id) {
        return $axios({
            url: `/lab/${id}`,
            method: 'delete'
        })
    }

    addLab(params) {
        return $axios({
            url: '/lab',
            method: 'post',
            data: {...params}
        })
    }

    editLab(params) {
        return $axios({
            url: '/lab',
            method: 'put',
            data: {...params}
        })
    }

    getAllLabList() {
        return $axios({
            url: '/lab/list',
            method: 'get',
        })
    }

    //User API
    getUserList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin: false
            }
        })
    }

    deleteUserById(id) {
        return $axios({
            url: `/user/${id}`,
            method: 'delete'
        })
    }

    changeUserPassword(id, params) {
        return $axios({
            url: '/user',
            method: 'put',
            data: {
                userId: id,
                userPassword: params
            }
        })
    }

    addUser(params) {
        return $axios({
            url: '/user',
            method: 'post',
            data: {
                ...params,
                roleId: 0
            }
        })
    }

    editUser(params) {
        return $axios({
            url: '/user',
            method: 'put',
            data: {
                ...params,
                userPassword: null
            }
        })
    }

    getAllUserList() {
        return $axios({
            url: '/user/list',
            method: 'get'
        })
    }

    //Manager API
    getManagerList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin: true
            }
        })
    }

    addManager(params) {
        return $axios({
            url: '/user',
            method: 'post',
            data: {
                ...params,
                roleId: 1
            }
        })
    }

}