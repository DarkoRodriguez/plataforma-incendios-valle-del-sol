import { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = `${import.meta.env.VITE_API_BASE || '/api'}/mapeo/reportes`;

export const useReportes = () => {
    const [reportes, setReportes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchReportes = async () => {
        setLoading(true);
        try {
            const response = await axios.get(API_URL);
            setReportes(response.data);
            setError(null);
        } catch (err) {
            setError(err.message || 'Error al obtener reportes');
        } finally {
            setLoading(false);
        }
    };

    const addReporte = async (reporte) => {
        try {
            const response = await axios.post(API_URL, reporte);
            setReportes([...reportes, response.data]);
            return true;
        } catch (err) {
            setError(err.message || 'Error al crear reporte');
            return false;
        }
    };

    useEffect(() => {
        fetchReportes();
    }, []);

    return { reportes, loading, error, addReporte, fetchReportes };
};
