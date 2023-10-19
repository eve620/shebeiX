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
