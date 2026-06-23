import { useState, useEffect } from 'react';
import { getFireReports, FireReportDTO } from '../services/api';

export const useReports = () => {
    const [reports, setReports] = useState<FireReportDTO[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchReports = async () => {
        setLoading(true);
        try {
            const data = await getFireReports();
            setReports(data);
            setError(null);
        } catch (err: any) {
            setError(err.message || 'Error fetching reports');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchReports();
    }, []);

    return { reports, loading, error, fetchReports };
};
