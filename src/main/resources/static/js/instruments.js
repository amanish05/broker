// instruments.js
// Loads instrument/exchange data for dropdowns and datalists
const nameTokenMap = {};
document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/instruments/exchanges')
        .then(r => r.json())
        .then(list => {
            const sel = document.getElementById('exchange-select');
            sel.innerHTML = list.map(e => `<option value="${e}">${e}</option>`).join('');
        });
    fetch('/api/instruments/names')
        .then(r => r.json())
        .then(list => {
            const dl = document.getElementById('name-list');
            dl.innerHTML = '';
            list.forEach(item => {
                nameTokenMap[item.name] = item.instrumentToken;
                const opt = document.createElement('option');
                opt.value = item.name;
                dl.appendChild(opt);
            });
        });
});
