// instruments.js
// Loads instrument/exchange data for dropdowns and datalists
const nameTokenMap = {};

function loadNames() {
    const ex = document.getElementById('exchange-select').value;
    const type = document.getElementById('type-select').value;
    fetch(`/api/instruments/names?exchange=${ex}&type=${type}`)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded names', list);
            const dl = document.getElementById('name-list');
            dl.innerHTML = '';
            for (const item of list) {
                nameTokenMap[item.name] = item.instrumentToken;
                const opt = document.createElement('option');
                opt.value = item.name;
                dl.appendChild(opt);
            }
        });
}

function loadTypes() {
    const ex = document.getElementById('exchange-select').value;
    fetch(`/api/instruments/types?exchange=${ex}`)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded types', list);
            const sel = document.getElementById('type-select');
            sel.innerHTML = list.map(t => `<option value="${t}">${t}</option>`).join('');
            loadNames();
        });
}

document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/instruments/exchanges')
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded exchanges', list);
            const sel = document.getElementById('exchange-select');
            sel.innerHTML = list.map(e => `<option value="${e}">${e}</option>`).join('');
            loadTypes();
        });
    document.getElementById('exchange-select').addEventListener('change', loadTypes);
    document.getElementById('type-select').addEventListener('change', loadNames);
    document.getElementById('refresh-btn').addEventListener('click', () => {
        const ex = document.getElementById('exchange-select').value;
        fetch(`/api/instruments/${ex}`, { method: 'POST' })
            .then(() => {
                console.info('Refresh complete for', ex);
                loadTypes();
            });
    });
});
