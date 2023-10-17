import getInstance from "@/sdk/Instance.js";

const instance = getInstance();

export default function downloadExcel(path, data, filename) {
    instance.exportExcel(path, data).then(res => {
        const blob = new Blob([res.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        window.URL.revokeObjectURL(url);
    });
}