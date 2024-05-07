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
    if (!mimeType) {
        return "未知"
    }
    if (mimeType.indexOf("image") === 0) {
        return "图片"
    }
    if (mimeType === "application/vnd.ms-powerpoint") {
        return "PPT"
    }
    if (mimeType === "dir") {
        return "文件夹"
    }
    return "文件"
}

export function formatDate(date) {
    return `${date.getFullYear()}-${formatInt(date.getMonth() + 1)}-${formatInt(date.getDate())} ${formatInt(date.getHours())}:${formatInt(date.getMinutes())}`
}

const formatInt = (num) => {
    return ("0" + num).slice(-2)
}



