export type RegionName =
  | 'Arica y Parinacota'
  | 'Tarapacá'
  | 'Antofagasta'
  | 'Atacama'
  | 'Coquimbo'
  | 'Valparaíso'
  | "Metropolitana de Santiago"
  | "O'Higgins"
  | "Maule"
  | 'Ñuble'
  | 'Biobío'
  | 'La Araucanía'
  | 'Los Ríos'
  | 'Los Lagos'
  | "Aysén del General Carlos Ibáñez del Campo"
  | "Magallanes y de la Antártica Chilena";

export const CHILE_REGIONS: RegionName[] = [
  'Arica y Parinacota',
  'Tarapacá',
  'Antofagasta',
  'Atacama',
  'Coquimbo',
  'Valparaíso',
  "Metropolitana de Santiago",
  "O'Higgins",
  "Maule",
  'Ñuble',
  'Biobío',
  'La Araucanía',
  'Los Ríos',
  'Los Lagos',
  "Aysén del General Carlos Ibáñez del Campo",
  "Magallanes y de la Antártica Chilena",
];

export const COMMUNES_BY_REGION: Record<RegionName, string[]> = {
  'Arica y Parinacota': ['Arica', 'Camarones', 'General Lagos', 'Putre'],
  'Tarapacá': ['Iquique', 'Alto Hospicio', 'Pozo Almonte', 'Camiña', 'Colchane', 'Huara', 'Pica'],
  'Antofagasta': ['Antofagasta', 'Mejillones', 'Sierra Gorda', 'Taltal', 'Calama', 'Ollagüe', 'San Pedro de Atacama', 'María Elena', 'Tocopilla'],
  'Atacama': ['Copiapó', 'Caldera', 'Tierra Amarilla', 'Chañaral', 'Diego de Almagro', 'Vallenar', 'Alto del Carmen', 'Freirina', 'Huasco'],
  'Coquimbo': ['La Serena', 'Coquimbo', 'Andacollo', 'La Higuera', 'Paiguano', 'Vicuña', 'Illapel', 'Canela', 'Los Vilos', 'Salamanca', 'Ovalle', 'Combarbalá', 'Monte Patria', 'Punitaqui', 'Río Hurtado'],
  'Valparaíso': ['Valparaíso', 'Viña del Mar', 'Concón', 'Quintero', 'Puchuncaví', 'Casablanca', 'San Antonio', 'Algarrobo', 'El Quisco', 'El Tabo', 'Cartagena', 'Santo Domingo', 'San Felipe', 'Santa María', 'Catemu', 'Llaillay', 'Panquehue', 'Putaendo', 'Quillota', 'La Calera', 'Hijuelas', 'La Cruz', 'Limache', 'Nogales', 'Villa Alemana', 'Quilpué', 'Olmué', 'Cabildo', 'La Ligua', 'Papudo', 'Petorca', 'Zapallar', 'Isla de Pascua', 'Juan Fernández'],
  'Metropolitana de Santiago': ['Santiago', 'Cerrillos', 'Cerro Navia', 'Conchalí', 'El Bosque', 'Estación Central', 'Huechuraba', 'Independencia', 'La Cisterna', 'La Florida', 'La Granja', 'La Reina', 'Las Condes', 'Lo Barnechea', 'Lo Prado', 'Macul', 'Maipú', 'Ñuñoa', 'Pedro Aguirre Cerda', 'Peñalolén', 'Providencia', 'Pudahuel', 'Quilicura', 'Quinta Normal', 'Renca', 'San Joaquín', 'San Miguel', 'San Ramón', 'Vitacura', 'Puente Alto', 'Pirque', 'San José de Maipo', 'Colina', 'Lampa', 'Tiltil', 'San Bernardo', 'Buin', 'Calera de Tango', 'Paine', 'Melipilla', 'Alhué', 'Curacaví', 'María Pinto', 'San Pedro', 'Talagante', 'El Monte', 'Isla de Maipo', 'Padre Hurtado', 'Peñaflor'],
  "O'Higgins": ["Rancagua", "Machalí", "Graneros", "Mostazal", "Olivar", "Doñihue", "Requínoa", "Coinco", "Coltauco", "Codegua", "Malloa", "Navidad", "Pichidegua", "Pichilemu", "Quinta de Tilcoco", "Rengo", "San Vicente de Tagua Tagua", "Litueche", "Marchihue", "Paredones", "San Fernando", "Chimbarongo", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz", "Lolol"],
  'Maule': ['Talca', 'San Clemente', 'Pelarco', 'Pencahue', 'Maule', 'Río Claro', 'Curepto', 'Constitución', 'Empedrado', 'Parral', 'Retiro', 'Cauquenes', 'Chanco', 'Pelluhue', 'Curicó', 'Hualañé', 'Licantén', 'Molina', 'Rauco', 'Romeral', 'Sagrada Familia', 'Teno', 'Vichuquén', 'Linares', 'Colbún', 'Longaví', 'San Javier', 'Villa Alegre', 'Yerbas Buenas'],
  'Ñuble': ['Chillán', 'Chillán Viejo', 'Bulnes', 'Cobquecura', 'Coelemu', 'Coihueco', 'El Carmen', 'Ninhue', 'Ñiquén', 'Pemuco', 'Pinto', 'Portezuelo', 'Quillón', 'Quirihue', 'Ránquil', 'San Carlos', 'San Fabián', 'San Ignacio', 'Tucapel', 'Yungay', 'Trehuaco'],
  'Biobío': ['Concepción', 'Coronel', 'Chiguayante', 'Florida', 'Hualpén', 'Hualqui', 'Lota', 'Penco', 'San Pedro de la Paz', 'Santa Juana', 'Talcahuano', 'Tomé', 'Alto Biobío', 'Antuco', 'Cabrero', 'Laja', 'Los Ángeles', 'Mulchén', 'Nacimiento', 'Negrete', 'Quilaco', 'Quilleco', 'San Rosendo', 'Santa Bárbara', 'Tucapel', 'Yumbel', 'Arauco', 'Cañete', 'Contulmo', 'Curanilahue', 'Lebu', 'Los Álamos', 'Tirúa'],
  'La Araucanía': ['Temuco', 'Padre Las Casas', 'Villarrica', 'Pitrufquén', 'Gorbea', 'Loncoche', 'Cunco', 'Curarrehue', 'Freire', 'Galvarino', 'Lautaro', 'Melipeuco', 'Nueva Imperial', 'Perquenco', 'Pucón', 'Saavedra', 'Teodoro Schmidt', 'Toltén', 'Vilcún', 'Carahue', 'Cholchol', 'Angol', 'Collipulli', 'Curacautín', 'Ercilla', 'Lonquimay', 'Los Sauces', 'Lumaco', 'Purén', 'Renaico', 'Traiguén', 'Victoria'],
  'Los Ríos': ['Valdivia', 'Corral', 'Lanco', 'Los Lagos', 'Máfil', 'Mariquina', 'La Unión', 'Río Bueno', 'Lago Ranco', 'Futrono', 'Paillaco', 'Panguipulli'],
  'Los Lagos': ['Puerto Montt', 'Castro', 'Ancud', 'Osorno', 'Puerto Varas', 'Calbuco', 'Cochamó', 'Fresia', 'Frutillar', 'Llanquihue', 'Los Muermos', 'Maullín', 'Purranque', 'Puerto Octay', 'Puyehue', 'Río Negro', 'San Pablo', 'San Juan de la Costa', 'Chaitén', 'Futaleufú', 'Hualaihué', 'Palena'],
  "Aysén del General Carlos Ibáñez del Campo": ["Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "O'Higgins", "Chile Chico", "Cochrane", "Río Ibáñez", "Tortel"],
  "Magallanes y de la Antártica Chilena": ["Punta Arenas", "Puerto Natales", "Torres del Paine", "Laguna Blanca", "Río Verde", "San Gregorio", "Porvenir", "Primavera", "Timaukel", "Cabo de Hornos", "Antártica Chilena"],
};
