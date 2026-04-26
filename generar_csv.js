const fs = require('fs');

const img_carnes = "https://images.unsplash.com/photo-1603048297172-c92544798d5e?auto=format&fit=crop&w=400&q=80";
const img_frutas = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=400&q=80";
const img_verduras = "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=400&q=80";
const img_pan = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80";
const img_quesos = "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?auto=format&fit=crop&w=400&q=80";
const img_fiambres = "https://images.unsplash.com/photo-1628294895950-9805252327bc?auto=format&fit=crop&w=400&q=80";
const img_bebidas = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=400&q=80";
const img_jugos = "https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=400&q=80";
const img_lacteos = "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=400&q=80";
const img_abarrotes = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80";
const img_snacks = "https://images.unsplash.com/photo-1566478989037-e624b0e4ce1f?auto=format&fit=crop&w=400&q=80";
const img_congelados = "https://images.unsplash.com/photo-1549488344-1f9b8d2bd1f3?auto=format&fit=crop&w=400&q=80";
const img_desayuno = "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?auto=format&fit=crop&w=400&q=80";
const img_aseo = "https://images.unsplash.com/photo-1584820927498-cafe2c2f6d2f?auto=format&fit=crop&w=400&q=80";
const img_perfumeria = "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=400&q=80";


const base = [
    // Feria
    ["Palta Hass Nacional", "Feria", 2500, 4990, 50, "Palta gran calibre", img_frutas],
    ["Tomate Larga Vida", "Feria", 800, 1500, 80, "Tomate rojo", img_verduras],
    ["Limón de Pica", "Feria", 1500, 2200, 30, "Especial para tragos", img_frutas],
    ["Cebolla Guarda", "Feria", 700, 1100, 100, "Cebolla firme", img_verduras],
    ["Papa Leona Blanca", "Feria", 500, 800, 150, "Cosecha nacional", img_verduras],
    ["Choclo Húngaro", "Feria", 400, 700, 40, "Choclo fresco", img_verduras],
    ["Manzana Fuji Crocante", "Feria", 900, 1400, 60, "Sabor dulce", img_frutas],
    ["Plátano Ecuador", "Feria", 1000, 1490, 90, "Importación premium", img_frutas],
    ["Lechuga Escarola Fresca", "Feria", 600, 990, 20, "Del día", img_verduras],
    ["Zanahoria a Granel", "Feria", 600, 900, 45, "Limpia", img_verduras],
    
    // Panadería
    ["Pan Marraqueta Blanca", "Panadería", 1300, 1990, 30, "Recién horneado", img_pan],
    ["Pan Hallulla Tradicional", "Panadería", 1300, 1990, 35, "Receta clásica", img_pan],
    ["Pan Coliza Campo", "Panadería", 1400, 2100, 10, "Coliza artesanal", img_pan],
    ["Pan Integral Fibra", "Panadería", 1500, 2300, 15, "Alto en fibra", img_pan],
    ["Pan Dobladitas Manteca", "Panadería", 1600, 2400, 12, "Excelente sabor", img_pan],
    ["Pre-Pizzas Caseras (Pack 2)", "Panadería", 1200, 1990, 20, "Masa a la piedra", img_pan],
    ["Empanadas Queso Horno", "Panadería", 700, 1200, 40, "Queso derretido", img_pan],
    ["Empanada Pino Horno", "Panadería", 900, 1500, 30, "Pino casero jugoso", img_pan],
    ["Sopaipillas Fritas (Kg)", "Panadería", 1800, 2990, 15, "Masa con zapallo", img_pan],
    ["Pan de Completo Ideal", "Panadería", 1200, 1890, 25, "10 Unidades", img_pan],
    
    // Carniceria
    ["Lomo Vetado Especial V", "Carnicería", 9500, 13990, 20, "Para asados", img_carnes],
    ["Posta Rosada Premium", "Carnicería", 7000, 9900, 25, "Cacerola", img_carnes],
    ["Huachalomo de Campo", "Carnicería", 6500, 8990, 30, "Económico asado", img_carnes],
    ["Abastero Vacuno", "Carnicería", 6800, 9200, 20, "Corte magro", img_carnes],
    ["Pechuga Pollo Entera", "Carnicería", 3500, 4990, 40, "Sin marinar", img_carnes],
    ["Trutro Entero Pollo", "Carnicería", 2800, 4100, 35, "A granel", img_carnes],
    ["Alitas de Pollo P", "Carnicería", 2500, 3990, 30, "Para barbacoa", img_carnes],
    ["Cerdo Chuleta Centro", "Carnicería", 4500, 6500, 20, "Sabrosa", img_carnes],
    ["Costillar Cerdo Sabroso", "Carnicería", 6000, 8500, 18, "Corte Nacional", img_carnes],
    ["Pulpa de Cerdo Magra", "Carnicería", 4200, 5990, 25, "Sin hueso", img_carnes],

    // Fiambrería
    ["Queso Gouda Soprole", "Fiambrería", 6500, 8990, 15, "Tradicional", img_quesos],
    ["Queso Mantecoso Llanquihue", "Fiambrería", 7000, 9500, 10, "Blanco fresco", img_quesos],
    ["Queso Cabra Fresco", "Fiambrería", 8500, 12000, 5, "Artesanal", img_quesos],
    ["Jamón Pierna San Jorge", "Fiambrería", 5500, 7900, 20, "Extra fino", img_fiambres],
    ["Jamón Pavo Receta del Abuelo", "Fiambrería", 7500, 9990, 12, "Dietético", img_fiambres],
    ["Salame Ahumado PF", "Fiambrería", 8500, 11500, 8, "Corte italiano", img_fiambres],
    ["Mortadela Lisa Sabrosa", "Fiambrería", 3500, 4990, 25, "Económica", img_fiambres],
    ["Arrollado Huaso Picante", "Fiambrería", 8000, 10500, 10, "Con ají", img_fiambres],
    ["Chorizo Parrillero Premium", "Fiambrería", 6000, 8500, 18, "Para asado", img_fiambres],
    ["Vienesa Tradicional Cerdo", "Fiambrería", 3800, 5200, 35, "Para hot dogs", img_fiambres],
    
    // Abarrotes1
    ["Aceite Maravilla Chef 1L", "Abarrotes", 1700, 2390, 120, "100% Maravilla", img_abarrotes],
    ["Aceite Belmont Vegetal 1L", "Abarrotes", 1600, 2190, 100, "Blend vegetal", img_abarrotes],
    ["Arroz Tucapel Largo 1Kg", "Abarrotes", 1200, 1690, 200, "El original", img_abarrotes],
    ["Arroz Miraflores Corto 1Kg", "Abarrotes", 1100, 1550, 150, "Grado 2", img_abarrotes],
    ["Fideos Carozzi Espagueti", "Abarrotes", 600, 890, 300, "N5", img_abarrotes],
    ["Fideos Lucchetti Corbatas", "Abarrotes", 650, 920, 250, "Clásicos cortos", img_abarrotes],
    ["Salsa de Tomate Pomarola", "Abarrotes", 350, 550, 200, "Salsa italiana", img_abarrotes],
    ["Atún Van Camps Lomitos", "Abarrotes", 1200, 1690, 120, "En agua", img_abarrotes],
    ["Jurel San José Tarro", "Abarrotes", 1100, 1590, 110, "Exportación", img_abarrotes],
    ["Azúcar Iansa Blanca 1Kg", "Abarrotes", 1000, 1350, 300, "Granulada", img_abarrotes],
    
    // Abarrotes2
    ["Sal de Mar Lobos 1Kg", "Abarrotes", 450, 650, 150, "Tradicional", img_abarrotes],
    ["Harina Mont Blanc Sin Polvos", "Abarrotes", 800, 1100, 140, "Panificación", img_abarrotes],
    ["Harina Selecta Con Polvos", "Abarrotes", 950, 1350, 120, "Repostería", img_abarrotes],
    ["Legumbres Lentejas 400g", "Abarrotes", 1100, 1600, 50, "Cosecha central", img_abarrotes],
    ["Porotos Tórtola 400g", "Abarrotes", 1300, 1800, 45, "Poroto nacional", img_abarrotes],
    ["Garbanzos Wasil Caja", "Abarrotes", 1000, 1450, 45, "Listos para servir", img_abarrotes],
    ["Mayonesa Hellmanns Pote", "Abarrotes", 1800, 2500, 60, "Receta casera", img_abarrotes],
    ["Ketchup Kraft Squeeze", "Abarrotes", 1400, 1990, 50, "Sabor americano", img_abarrotes],
    ["Mostaza JB Tradicional", "Abarrotes", 900, 1300, 40, "Clásica chilena", img_abarrotes],
    ["Aceto Balsámico Chef", "Abarrotes", 2100, 2900, 15, "Aliño premium", img_abarrotes],

    // Desayuno
    ["Café Nescafé Tradición 100g", "Desayuno", 2500, 3490, 80, "Instantáneo", img_desayuno],
    ["Té Supremo Ceylan 100", "Desayuno", 1900, 2690, 120, "Té negro", img_desayuno],
    ["Milo Nestlé Tarro 400g", "Desayuno", 2800, 3990, 50, "Bebida achocolatada", img_desayuno],
    ["Avena Quaker Tradicional", "Desayuno", 1200, 1690, 70, "Avena integral", img_desayuno],
    ["Cereal Chocapic Nestlé", "Desayuno", 2500, 3600, 80, "Chocolate crocante", img_desayuno],
    ["Leche Condensada Nestlé", "Desayuno", 1300, 1850, 90, "Original", img_desayuno],
    ["Manjar Colun Bolsa", "Desayuno", 1200, 1750, 60, "Receta campo", img_desayuno],
    ["Mantequilla Colun 250g", "Desayuno", 2200, 2990, 60, "Del sur con sal", img_desayuno],
    ["Margarina Soprole Pote", "Desayuno", 1300, 1890, 90, "Untable", img_desayuno],
    ["Mermelada Watts Durazno", "Desayuno", 900, 1290, 60, "Frutilla natural", img_desayuno],

    // Lacteos
    ["Leche Colun Entera Caja", "Lácteos", 900, 1250, 120, "Grasa entera", img_lacteos],
    ["Leche Soprole Descremada", "Lácteos", 850, 1200, 100, "Baja en grasas", img_lacteos],
    ["Leche Soprole Sin Lactosa", "Lácteos", 1100, 1500, 90, "Fácil digestión", img_lacteos],
    ["Yoghurt Soprole Batido", "Lácteos", 250, 380, 200, "Frutilla", img_lacteos],
    ["Yoghurt Colun Trozos", "Lácteos", 350, 500, 150, "Durazno natural", img_lacteos],
    ["Yoghurt Protein Plus", "Lácteos", 550, 850, 80, "Aumento muscular", img_lacteos],
    ["Quesillo Colun Regular", "Lácteos", 1800, 2490, 40, "Fresco dietético", img_lacteos],
    ["Postre Manjarate Pote", "Lácteos", 650, 950, 40, "Mousse tradicional", img_lacteos],
    ["Probiótico Chamyto Pack", "Lácteos", 1400, 2100, 45, "Para las defensas", img_lacteos],
    ["Crema Soprole 200ml", "Lácteos", 950, 1350, 80, "Para cocinar o batir", img_lacteos],

    // Bebidas
    ["Coca-Cola Original 3L", "Bebidas", 2300, 3100, 150, "Clásica azucarada", img_bebidas],
    ["Coca-Cola Zero 3L", "Bebidas", 2300, 3100, 120, "Sin azúcar", img_bebidas],
    ["Sprite Limón 3L", "Bebidas", 2100, 2800, 80, "Refrescante", img_bebidas],
    ["Fanta Naranja 3L", "Bebidas", 2100, 2800, 60, "Naranja burbujas", img_bebidas],
    ["Kem Piña 1.5L", "Bebidas", 1200, 1650, 50, "Cultura chilena pop", img_bebidas],
    ["Papaya Pap 1.5L", "Bebidas", 1200, 1650, 50, "Nacional auténtica", img_bebidas],
    ["Canada Dry Ginger Ale", "Bebidas", 1500, 2100, 40, "Para piscolas premium", img_bebidas],
    ["Jugo Watts Durazno 1.5L", "Bebidas", 1300, 1750, 90, "Néctar botella", img_jugos],
    ["Agua Cachantun Gas L", "Bebidas", 800, 1150, 120, "Mineral gasificada", img_bebidas],
    ["Agua Vital Sin Gas L", "Bebidas", 750, 1050, 110, "Agua purificada", img_bebidas],

    // Cervezas y Licores
    ["Cerveza Cristal Lata", "Bebidas", 600, 950, 200, "Refrescante clásica", img_bebidas],
    ["Cerveza Escudo Lata", "Bebidas", 650, 1000, 150, "Más grado", img_bebidas],
    ["Cerveza Royal Guard Pack", "Bebidas", 4500, 5990, 30, "Premium", img_bebidas],
    ["Cerveza Becker Lata", "Bebidas", 550, 900, 180, "Económica asados", img_bebidas],
    ["Cerveza Corona Vidrio", "Bebidas", 1100, 1500, 90, "Importada mexico", img_bebidas],
    ["Pisco Mistral 35", "Bebidas", 6500, 8900, 20, "Añejado roble", img_bebidas],
    ["Pisco Alto del Carmen", "Bebidas", 6200, 8500, 25, "Reservado transparente", img_bebidas],
    ["Vino Casillero Diablo", "Bebidas", 3500, 4800, 40, "Carmenere Valle", img_bebidas],
    ["Vino Gato Negro Tinto", "Bebidas", 2500, 3500, 50, "Vino de mesa popular", img_bebidas],
    ["Energética Red Bull", "Bebidas", 1200, 1800, 80, "Revitalizante 250ml", img_bebidas],

    // Snacks
    ["Papas Lays Clásicas", "Snacks", 1800, 2400, 60, "Corte liso", img_snacks],
    ["Ramitas de Queso Evercrisp", "Snacks", 1500, 2100, 50, "Cumpleaños", img_snacks],
    ["Doritos Queso Grande", "Snacks", 1600, 2200, 40, "Tortillas crocantes", img_snacks],
    ["Mani Salado Marco Polo", "Snacks", 1100, 1500, 70, "Ideal pichangas", img_snacks],
    ["Galletas Tritón Vainilla", "Snacks", 600, 850, 100, "Clásicas bañadas", img_snacks],
    ["Galletas Tuareg Coco", "Snacks", 650, 900, 80, "Sabor intenso", img_snacks],
    ["Galletas McKay Vino", "Snacks", 500, 750, 120, "Tradicional colación", img_snacks],
    ["Super 8 Nestlé Caja", "Snacks", 4500, 6000, 15, "Obsequio clásico", img_snacks],
    ["Chocolate Costa Rama", "Snacks", 1800, 2400, 30, "Hojuelas", img_snacks],
    ["Bombones Vizzio", "Snacks", 2200, 2900, 30, "Almendras cubiertas", img_snacks],

    // Aseo
    ["Papel Higiénico Confort", "Aseo", 2800, 3900, 60, "Económico 40m", img_aseo],
    ["Papel Toalla Nova", "Aseo", 1800, 2500, 80, "Seca todo 2x", img_aseo],
    ["Lavalozas Quix Limón", "Aseo", 1400, 1990, 100, "Desengrasante", img_aseo],
    ["Detergente Omo Polvo", "Aseo", 2500, 3600, 40, "Multiuso matic", img_aseo],
    ["Detergente Ariel Líquido", "Aseo", 4500, 6200, 30, "Quitamanchas 1L", img_aseo],
    ["Suavizante Fuzol Ropa", "Aseo", 1500, 2200, 50, "Brisa floral", img_aseo],
    ["Cloro Tradicional Gel", "Aseo", 900, 1300, 120, "Limpieza profunda", img_aseo],
    ["Limpiador Poett Lavanda", "Aseo", 1600, 2250, 70, "Pisos brillantes", img_aseo],
    ["Bolsas Basura Virutex", "Aseo", 1200, 1800, 60, "Tamaño mediano", img_aseo],
    ["Esponja Scotch-Brite", "Aseo", 1800, 2500, 50, "Cuidado fácil", img_aseo],

    // Perfumeria
    ["Pasta Dental Pepsodent", "Perfumería", 1300, 1850, 100, "Blanqueadora", img_perfumeria],
    ["Pasta Dental Colgate Total", "Perfumería", 1800, 2500, 80, "Sarro total", img_perfumeria],
    ["Cepillo Dientes Oral-B", "Perfumería", 1500, 2100, 60, "Cerdas suaves", img_perfumeria],
    ["Jabón Barra Ballerina", "Perfumería", 600, 900, 150, "Avena humectante", img_perfumeria],
    ["Jabón Líquido Dove", "Perfumería", 2500, 3500, 40, "Neutro pH", img_perfumeria],
    ["Shampoo Head & Shoulders", "Perfumería", 3800, 5300, 45, "Anticaspa", img_perfumeria],
    ["Shampoo Ballerina Doy", "Perfumería", 1800, 2600, 50, "Económico envase", img_perfumeria],
    ["Acondicionador Sedal Brillo", "Perfumería", 2600, 3600, 35, "Revitalizante", img_perfumeria],
    ["Desodorante Spray Rexona", "Perfumería", 2800, 3990, 50, "Motion sense hombre", img_perfumeria],
    ["Desodorante Roll-On Dove", "Perfumería", 2200, 3100, 40, "Piel sensible", img_perfumeria],

    // Congelados
    ["Papas Prefritas Verde", "Congelados", 2500, 3490, 40, "Corte tradicional", img_congelados],
    ["Hamburguesas Receta Abuelo", "Congelados", 4500, 6500, 20, "100g vacuno", img_congelados],
    ["Helado Bresler Almendrado", "Congelados", 900, 1300, 40, "Palta vainilla almendra", img_congelados],
    ["Helado Savory Cassata 1L", "Congelados", 2800, 3990, 15, "Tres leches", img_congelados],
    ["Sofrito Mix Congelado", "Congelados", 1200, 1690, 35, "Cebolla zanahoria", img_congelados],
    ["Choclo Grano Verde", "Congelados", 1800, 2500, 30, "Desgranado", img_congelados],
    ["Empanadas Queso Congeladas", "Congelados", 3500, 4990, 15, "10 unidades", img_congelados],
    ["Pizza Piedra Pepperoni", "Congelados", 4500, 6200, 20, "Horno rápido", img_congelados],
    ["Churros Prefritos (Bolsa)", "Congelados", 3200, 4500, 25, "Rellenar con manjar", img_congelados],
    ["Arvejas Minuto Verde", "Congelados", 1500, 2100, 35, "Listas para cocer", img_congelados]
];


let csvContent = "ID,NOMBRE,CATEGORIA,PRECIO_COMPRA,PRECIO_VENTA,STOCK,DESCRIPCION,URL_IMAGEN\n";

let idCounter = 1;
base.forEach((item) => {
    // Si la coma rompió algo en la URL original (acá no sucederá), las quitamos
    csvContent += `${idCounter},${item[0]},${item[1]},${item[2]},${item[3]},${item[4]},"${item[5]}",${item[6]}\n`;
    idCounter++;
});

// Paths
const desktopPath1 = "C:\\Users\\gomez\\OneDrive\\Desktop\\MARKET GOMEZ\\catalogo_market_gomez_systems_final.csv";
const desktopPath2 = "C:\\Users\\gomez\\OneDrive\\Desktop\\catalogo_market_gomez_systems_final.csv";

try { fs.writeFileSync(desktopPath1, csvContent, 'utf-8'); } catch(e){}
try { fs.writeFileSync(desktopPath2, csvContent, 'utf-8'); } catch(e){}

console.log("Generado catalog con fotos HD absolutas", idCounter - 1);
