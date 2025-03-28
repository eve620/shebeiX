import {createApp} from 'vue'
import router from './router'
import App from './App.vue'
import * as Icons from '@ant-design/icons-vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import uploader from 'vue-simple-uploader'
import 'vue-simple-uploader/dist/style.css'

const app = createApp(App)

for (const key in Icons) {
    app.component(key, Icons[key])
}
app.use(Antd)
app.use(router)
app.use(uploader)
app.mount('#app')