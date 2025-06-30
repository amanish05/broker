// instruments.js
// Enhanced instrument management with filtering and live updates
const nameTokenMap = {};
let allInstruments = []; // Store all instruments for filtering
window.nameTokenMap = nameTokenMap; // Make globally accessible

function initializeInstruments() {
    // Called after configuration is loaded
    console.log('Instruments module initialized with config');
    setupInstrumentFiltering();
    loadExchangesAndTypes();
}

function setupInstrumentFiltering() {
    const nameInput = document.getElementById('api-input');
    const datalist = document.getElementById('name-list');
    
    if (!nameInput || !datalist) return;
    
    // Add real-time filtering as user types
    nameInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        datalist.innerHTML = '';
        
        if (searchTerm.length < 2) {
            // Show top instruments if search is too short
            const limit = getConfig('data.topInstrumentsLimit', 10);
            const topInstruments = allInstruments.slice(0, limit);
            topInstruments.forEach(item => {
                const option = document.createElement('option');
                option.value = item.name;
                option.textContent = `${item.name} (${item.exchange}${item.type ? ' - ' + item.type : ''})`;
                datalist.appendChild(option);
            });
            return;
        }
        
        // Filter instruments based on search term
        const filtered = allInstruments
            .filter(item => item.name.toLowerCase().includes(searchTerm))
            .slice(0, getConfig('data.searchResultsLimit', 20)); // Limit results
            
        filtered.forEach(item => {
            const option = document.createElement('option');
            option.value = item.name;
            option.textContent = `${item.name} (${item.exchange}${item.type ? ' - ' + item.type : ''})`;
            datalist.appendChild(option);
        });
        
        console.debug(`Filtered ${filtered.length} instruments for "${searchTerm}"`);
    });
    
    // Handle selection
    nameInput.addEventListener('change', function() {
        const selectedName = this.value;
        const selectedInstrument = allInstruments.find(item => item.name === selectedName);
        
        if (selectedInstrument) {
            nameTokenMap[selectedName] = selectedInstrument.instrumentToken;
            console.debug('Selected instrument:', selectedInstrument);
        }
    });
}

function loadAllInstruments() {
    console.info('Loading all available instruments...');
    
    // Load instruments from all exchanges with multiple types
    const exchanges = getConfig('exchanges.defaults', ['NSE', 'BSE']);
    const types = getConfig('instruments.defaultTypes', ['Options']); // Now defaults to Options only
    
    const promises = [];
    
    exchanges.forEach(exchange => {
        types.forEach(type => {
            promises.push(
                fetch(`/api/instruments/names?exchange=${exchange}&type=${type}`)
                    .then(r => r.ok ? r.json() : [])
                    .catch(err => {
                        console.warn(`Failed to load ${type} instruments from ${exchange}:`, err);
                        return [];
                    })
            );
        });
    });
    
    Promise.all(promises)
        .then(results => {
            // Combine all instruments
            allInstruments = [];
            results.forEach((instrumentList) => {
                instrumentList.forEach(item => {
                    const instrumentName = item.name || item.tradingsymbol;
                    // Add type information to the instrument for better identification
                    allInstruments.push({
                        name: instrumentName,
                        instrumentToken: item.instrumentToken,
                        exchange: item.exchange || 'NSE', // Default to NSE if not specified
                        type: item.instrumentType || 'Unknown'
                    });
                    // Also update the token map
                    nameTokenMap[instrumentName] = item.instrumentToken;
                });
            });
            
            console.info(`Loaded ${allInstruments.length} total instruments`);
            
            // Setup filtering after loading data
            setupInstrumentFiltering();
            
            // Populate initial datalist with popular instruments
            populateInitialInstruments();
        })
        .catch(err => {
            console.error('Failed to load instruments:', err);
        });
}

function populateInitialInstruments() {
    const datalist = document.getElementById('name-list');
    if (!datalist) return;
    
    // Show popular instruments by default, prioritizing options
    const popularOptions = allInstruments
        .filter(item => item.name.includes('NIFTY') || item.name.includes('BANKNIFTY'))
        .slice(0, 6);
    
    const popularEquities = allInstruments
        .filter(item => ['RELIANCE', 'TCS', 'INFY', 'HDFC', 'ICICIBANK']
            .some(symbol => item.name.includes(symbol)) && item.type === 'EQ')
        .slice(0, 4);
    
    const popularInstruments = [...popularOptions, ...popularEquities];
    
    datalist.innerHTML = '';
    popularInstruments.forEach(item => {
        const option = document.createElement('option');
        option.value = item.name;
        option.textContent = `${item.name} (${item.exchange}${item.type ? ' - ' + item.type : ''})`;
        datalist.appendChild(option);
    });
    
    console.debug(`Populated ${popularInstruments.length} popular instruments (${popularOptions.length} options, ${popularEquities.length} equities)`);
}

function loadNames() {
    const ex = document.getElementById('exchange-select').value;
    const type = document.getElementById('type-select').value;
    
    if (!ex || !type) return;
    
    fetch(`/api/instruments/names?exchange=${ex}&type=${type}`)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded names for', ex, type, ':', list.length, 'instruments');
            const dl = document.getElementById('name-list');
            dl.innerHTML = '';
            
            for (const item of list) {
                const name = item.name || item.tradingsymbol;
                nameTokenMap[name] = item.instrumentToken;
                const opt = document.createElement('option');
                opt.value = name;
                opt.textContent = `${name} (${ex})`;
                dl.appendChild(opt);
            }
        })
        .catch(err => {
            console.error('Failed to load names:', err);
        });
}

function loadTypes() {
    const ex = document.getElementById('exchange-select').value;
    if (!ex) return;
    
    fetch(`/api/instruments/types?exchange=${ex}`)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded types for', ex, ':', list);
            const sel = document.getElementById('type-select');
            sel.innerHTML = '';
            
            // Sort types to prioritize options and futures
            const sortedTypes = list.sort((a, b) => {
                const priority = { 'CE': 1, 'PE': 2, 'FUT': 3, 'EQ': 4 };
                return (priority[a] || 99) - (priority[b] || 99);
            });
            
            let defaultSet = false;
            
            // Add all available types from the exchange
            sortedTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type;
                
                // Create descriptive text for each type
                switch(type) {
                    case 'CE':
                        option.textContent = 'Call Options (CE)';
                        if (!defaultSet) {
                            option.selected = true;
                            defaultSet = true;
                        }
                        break;
                    case 'PE':
                        option.textContent = 'Put Options (PE)';
                        if (!defaultSet) {
                            option.selected = true;
                            defaultSet = true;
                        }
                        break;
                    case 'FUT':
                        option.textContent = 'Futures (FUT)';
                        if (!defaultSet) {
                            option.selected = true;
                            defaultSet = true;
                        }
                        break;
                    case 'EQ':
                        option.textContent = 'Equity (EQ)';
                        break;
                    default:
                        option.textContent = type;
                        break;
                }
                
                sel.appendChild(option);
            });
            
            // If no options/futures found, default to first available type
            if (!defaultSet && sortedTypes.length > 0) {
                sel.children[0].selected = true;
            }
            
            loadNames();
        })
        .catch(err => {
            console.error('Failed to load types:', err);
        });
}

function refreshInstruments() {
    console.info('Refreshing instruments from broker API...');
    
    // Show loading state
    const refreshBtn = document.getElementById('refresh-btn');
    const originalText = refreshBtn.textContent;
    refreshBtn.disabled = true;
    refreshBtn.textContent = 'Refreshing...';
    
    fetch('/api/instruments/refresh', { 
        method: 'POST',
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.info('Refresh complete - Updated:', data.totalUpdated, 'instruments across', data.exchangesProcessed, 'exchanges');
        
        // Show success message
        const resultDiv = document.getElementById('api-action-result');
        if (resultDiv) {
            resultDiv.innerHTML = `<div style="background:#d4edda;color:#155724;padding:8px;border-radius:4px;margin-top:8px;">
                ✅ ${data.message}: ${data.totalUpdated} instruments across ${data.exchangesProcessed} exchanges
            </div>`;
            
            // Clear message after 5 seconds
            setTimeout(() => {
                resultDiv.innerHTML = '';
            }, 5000);
        }
        
        // Reload all data
        loadTypes();
        loadUnderlyingAssets();
        loadAllInstruments();
    })
    .catch(error => {
        console.error('Refresh failed:', error);
        
        // Show error message
        const resultDiv = document.getElementById('api-action-result');
        if (resultDiv) {
            resultDiv.innerHTML = `<div style="background:#f8d7da;color:#721c24;padding:8px;border-radius:4px;margin-top:8px;">
                ❌ Failed to refresh instruments: ${error.message}
            </div>`;
        }
    })
    .finally(() => {
        // Restore button state
        refreshBtn.disabled = false;
        refreshBtn.textContent = originalText;
    });
}

function loadUnderlyingAssets() {
    console.info('Loading underlying assets using V2 API...');
    
    // Use new consolidated V2 API endpoint
    fetch('/api/v2/instruments/metadata/underlyings')
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded underlying assets via V2 API:', list);
            const select = document.getElementById('underlying-select');
            if (!select) return;
            
            // Clear existing options except the placeholder
            select.innerHTML = '<option value="">Select underlying asset...</option>';
            
            list.forEach(underlying => {
                const option = document.createElement('option');
                option.value = underlying;
                option.textContent = underlying;
                select.appendChild(option);
            });
            
            console.info(`Loaded ${list.length} underlying assets via V2 API`);
        })
        .catch(err => {
            console.warn('V2 API failed, falling back to legacy API:', err);
            // Fallback to legacy API for backward compatibility
            fetch('/api/instruments/underlyings')
                .then(r => r.json())
                .then(list => {
                    console.debug('Loaded underlying assets via legacy API:', list);
                    const select = document.getElementById('underlying-select');
                    if (!select) return;
                    
                    select.innerHTML = '<option value="">Select underlying asset...</option>';
                    list.forEach(underlying => {
                        const option = document.createElement('option');
                        option.value = underlying;
                        option.textContent = underlying;
                        select.appendChild(option);
                    });
                    
                    console.info(`Loaded ${list.length} underlying assets via legacy API`);
                })
                .catch(legacyErr => {
                    console.error('Both V2 and legacy APIs failed:', legacyErr);
                });
        });
}

function loadExpiryDates() {
    const underlying = document.getElementById('underlying-select').value;
    if (!underlying) {
        // Clear expiry dropdown
        const expirySelect = document.getElementById('expiry-select');
        if (expirySelect) {
            expirySelect.innerHTML = '<option value="">Select expiry date...</option>';
        }
        return;
    }
    
    console.info('Loading expiry dates for underlying:', underlying);
    
    fetch(`/api/instruments/expiry-dates?underlying=${encodeURIComponent(underlying)}`)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded expiry dates for', underlying, ':', list);
            const select = document.getElementById('expiry-select');
            if (!select) return;
            
            // Clear existing options except the placeholder
            select.innerHTML = '<option value="">Select expiry date...</option>';
            
            list.forEach(expiry => {
                const option = document.createElement('option');
                option.value = expiry;
                option.textContent = new Date(expiry).toLocaleDateString('en-IN');
                select.appendChild(option);
            });
            
            console.info(`Loaded ${list.length} expiry dates for ${underlying}`);
        })
        .catch(err => {
            console.error('Failed to load expiry dates for', underlying, ':', err);
        });
}

function loadFilteredInstruments() {
    const underlying = document.getElementById('underlying-select').value;
    const expiry = document.getElementById('expiry-select').value;
    
    if (!underlying) {
        // No underlying selected, load all instruments
        loadAllInstruments();
        return;
    }
    
    let url = `/api/instruments/by-underlying?underlying=${encodeURIComponent(underlying)}`;
    if (expiry) {
        url = `/api/instruments/by-underlying-expiry?underlying=${encodeURIComponent(underlying)}&expiry=${expiry}`;
    }
    
    console.info('Loading filtered instruments for', underlying, expiry ? `expiry ${expiry}` : '');
    
    fetch(url)
        .then(r => r.json())
        .then(list => {
            console.debug('Loaded filtered instruments:', list.length, 'items');
            
            // Update the datalist with filtered instruments
            const datalist = document.getElementById('name-list');
            if (!datalist) return;
            
            datalist.innerHTML = '';
            
            list.slice(0, getConfig('data.filteredResultsLimit', 50)).forEach(instrument => { // Limit results
                const option = document.createElement('option');
                option.value = instrument.name || instrument.tradingsymbol;
                option.textContent = `${instrument.name || instrument.tradingsymbol} (${instrument.instrumentType || 'Unknown'})`;
                datalist.appendChild(option);
                
                // Update token map
                nameTokenMap[instrument.name || instrument.tradingsymbol] = instrument.instrumentToken;
            });
            
            console.info(`Populated ${Math.min(50, list.length)} filtered instruments in search dropdown`);
        })
        .catch(err => {
            console.error('Failed to load filtered instruments:', err);
        });
}

function initializeExchangeDropdown() {
    const exchangeSelect = document.getElementById('exchange-select');
    if (!exchangeSelect) return;
    
    // Clear existing options
    exchangeSelect.innerHTML = '';
    
    // Add NSE as default (selected)
    const nseOption = document.createElement('option');
    nseOption.value = 'NSE';
    nseOption.textContent = 'NSE (National Stock Exchange)';
    nseOption.selected = true;
    exchangeSelect.appendChild(nseOption);
    
    // Add BSE
    const bseOption = document.createElement('option');
    bseOption.value = 'BSE';
    bseOption.textContent = 'BSE (Bombay Stock Exchange)';
    exchangeSelect.appendChild(bseOption);
    
    console.info('Initialized exchange dropdown with NSE as default');
}

document.addEventListener('DOMContentLoaded', function() {
    console.info('Initializing instruments module...');
    
    // Initialize exchange dropdown with defaults
    initializeExchangeDropdown();
    
    // Setup event listeners
    const exchangeSelect = document.getElementById('exchange-select');
    const typeSelect = document.getElementById('type-select');
    const underlyingSelect = document.getElementById('underlying-select');
    const expirySelect = document.getElementById('expiry-select');
    const refreshBtn = document.getElementById('refresh-btn');
    
    if (exchangeSelect) {
        exchangeSelect.addEventListener('change', loadTypes);
    }
    
    if (typeSelect) {
        typeSelect.addEventListener('change', loadNames);
    }
    
    if (underlyingSelect) {
        underlyingSelect.addEventListener('change', function() {
            loadExpiryDates();
            loadFilteredInstruments();
        });
    }
    
    if (expirySelect) {
        expirySelect.addEventListener('change', loadFilteredInstruments);
    }
    
    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshInstruments);
    }
    
    // Load initial data
    loadTypes();
    loadUnderlyingAssets();
    
    // Load all instruments for filtering (after a short delay)
    setTimeout(loadAllInstruments, 1000);
});
