import React from 'react';
import { useReportes } from './hooks/useReportes';
import { MapView } from './components/MapView';

function App() {
    const { reportes, loading, error, addReporte } = useReportes();

    return (
        <div style={{ fontFamily: 'Arial, sans-serif', backgroundColor: '#f5f5f5', minHeight: '100vh', padding: '20px' }}>
            <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
                <header style={{ marginBottom: '20px', textAlign: 'center', color: '#d32f2f' }}>
                    <h1>Municipalidad Valle del Sol</h1>
                    <h2>Sistema de Prevención de Incendios</h2>
                </header>

                {error && (
                    <div style={{ backgroundColor: '#ffebee', color: '#c62828', padding: '10px', borderRadius: '4px', marginBottom: '15px' }}>
                        Error: {error}
                    </div>
                )}

                <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <MapView reportes={reportes} onReportar={addReporte} />
                    {loading && <p style={{ textAlign: 'center', marginTop: '10px' }}>Cargando reportes...</p>}
                </div>
            </div>
        </div>
    );
}

export default App;
