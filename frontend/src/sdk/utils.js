import {encrypt, decrypt} from 'crypto-js/aes'
import pkcs7 from 'crypto-js/pad-pkcs7'
import ECB from 'crypto-js/mode-ecb'
import UTF8 from 'crypto-js/enc-utf8'

// 注意 key 和 iv 至少都需要 16 位
const AES_KEY = '1111111111000000'
const AES_IV = '0000001111111111'

export function encryptByAES(text) {
    return encrypt(text, AES_KEY, {
        mode: ECB,
        padding: pkcs7,
        iv: AES_IV,
    }).toString()
}

export function decryptByAES(text) {
    return decrypt(text, AES_KEY, {
        mode: ECB,
        padding: pkcs7,
        iv: AES_IV,
    }).toString(UTF8)
}

export function formatBytes(byteLen) {
    if (!byteLen) {
        return ""
    }
    const units = ["B", "KB", "MB", "GB", "TB"]
    let index = 0
    while (index < units.length) {
        if (byteLen < 1024) {
            break;
        }
        byteLen = byteLen / 1024
        index++
    }
    return Math.ceil(byteLen * 100) / 100 + " " + units[index];
}

export function getFileType(mimeType) {
    if (!mimeType) return "未知"
    const mimeTypeMap = {
        'application/x-zip-compressed': 'zip',
        'application/pdf': 'pdf',
        'image/jpeg': 'jpg',
        'image/png': 'png',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
        'application/octet-stream': 'rar'
        // 可以继续添加更多映射...
    };

    // 从映射对象中查找给定的MIME类型，如果找到则返回对应的通俗类型，否则返回原MIME类型
    return mimeTypeMap[mimeType] || mimeType;
}

export function formatDate(date) {
    return `${date.getFullYear()}-${formatInt(date.getMonth() + 1)}-${formatInt(date.getDate())} ${formatInt(date.getHours())}:${formatInt(date.getMinutes())}`
}

const formatInt = (num) => {
    return ("0" + num).slice(-2)
}

export function deletePrefixSlash(str) {
    return str.indexOf("/") === 0 ? str.substring(1) : str
}

export function pathJoin(dir, file) {
    if (dir === "" || dir == null) {
        return file
    }
    return dir + "/" + file
}

export function validateDirOrFileName(name) {
    // 检查名字是否为空
    if (!name) {
        return false;
    }

    // 检查名字是否包含空格
    if (/\s/.test(name)) {
        return false;
    }

    // 检查名字是否包含特殊字符
    // 这里我们定义了一些常见的特殊字符
    const specialChars = /[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/;
    if (specialChars.test(name)) {
        return false;
    }

    // 如果没有问题，则返回 true
    return true;
}
