import csv
import os

productos = [
    # FERIA (Kilo)
    ["Palta Hass", "Feria", 3500, 4990, 50, "Palta gran calibre origen peruano", "avocado"],
    ["Tomate Larga Vida", "Feria", 800, 1200, 80, "Tomate rojo primera selección", "tomato"],
    ["Limón de Pica", "Feria", 1500, 2200, 30, "Limón de pica ideal para tragos", "lamon"],
    ["Cebolla Guarda", "Feria", 700, 1100, 100, "Cebolla de campo firme", "onion"],
    ["Papa Leona", "Feria", 500, 800, 150, "Papa seleccionada chilena fresca", "potato"],
    ["Choclo Húngaro", "Feria", 400, 700, 40, "Choclo grande tierno", "corn"],
    ["Manzana Fuji", "Feria", 900, 1400, 60, "Manzana dulce crocante", "apple"],
    ["Plátano Ecuador", "Feria", 1000, 1490, 90, "Plátano amarillo importado", "banana"],
    ["Lechuga Escarola", "Feria", 600, 990, 20, "Lechuga fresca del día", "lettuce"],
    ["Zanahoria a Granel", "Feria", 600, 900, 45, "Zanahoria limpia sin hojas", "carrot"],
    ["Uva Red Globe", "Feria", 1800, 2690, 25, "Uva extra dulce", "grapes"],
    ["Naranja Nacional", "Feria", 850, 1200, 70, "Para jugo fresco", "orange"],
    ["Pimentón Rojo", "Feria", 600, 900, 30, "Pimentón primera unidad", "pepper"],
    ["Ajo Chilote", "Feria", 200, 450, 100, "Cabeza de ajo grande", "garlic"],
    ["Frutilla Cesta", "Feria", 1800, 2990, 20, "Cesta 500g frutilla de la zona", "strawberry"],

    # ABARROTES
    ["Aceite Maravilla Natura 1L", "Abarrotes", 1700, 2390, 120, "Aceite 100% Maravilla", "cooking,oil"],
    ["Aceite Belmont Vegetal 1L", "Abarrotes", 1600, 2190, 100, "Aceite blend vegetal", "oil,bottle"],
    ["Arroz Tucapel Grano Largo 1Kg", "Abarrotes", 1200, 1690, 200, "El original", "rice"],
    ["Arroz Miraflores G. Corto 1Kg", "Abarrotes", 1100, 1550, 150, "Grado 2 tradicional", "rice,bowl"],
    ["Fideos Carozzi Espagueti 400g", "Abarrotes", 600, 890, 300, "Pasta clásica n5", "spaghetti"],
    ["Fideos Lucchetti Tallarín 400g", "Abarrotes", 650, 920, 250, "Fideos chilenos", "pasta"],
    ["Salsa de Tomate Pomarola 250g", "Abarrotes", 350, 500, 200, "Salsa de tomate italiana", "tomato,sauce"],
    ["Atún Van Camp's Lomitos 160g", "Abarrotes", 1200, 1690, 120, "Lomitos en agua", "canned,tuna"],
    ["Atún Robinson Crusoe Aceite 160g", "Abarrotes", 1400, 1990, 90, "En aceite vegetal", "canned,food"],
    ["Jurel San José Tarro 425g", "Abarrotes", 1100, 1590, 110, "Tipo exportación", "fish,can"],
    ["Azúcar Iansa Blanca 1Kg", "Abarrotes", 1000, 1350, 300, "Azúcar blanca granulada", "sugar"],
    ["Azúcar Iansa Rubia 500g", "Abarrotes", 800, 1100, 80, "Azúcar rubia sin refinar", "brown,sugar"],
    ["Sal de Mar Lobos 1Kg", "Abarrotes", 450, 650, 150, "Sal tradicional", "salt"],
    ["Salero Biosal 250g", "Abarrotes", 900, 1300, 40, "Menos sodio", "salt,shaker"],
    ["Harina Mont Blanc Sin Polvos 1Kg", "Abarrotes", 800, 1100, 140, "Harina de trigo tradicional", "flour"],
    ["Harina Selecta Con Polvos 1Kg", "Abarrotes", 950, 1350, 120, "Harina lista para repostería", "baking,flour"],
    ["Leche Condensada Nestlé Tarro", "Abarrotes", 1300, 1850, 90, "Original azucarada", "condensed,milk"],
    ["Manjar Colun Bolsa 400g", "Abarrotes", 1200, 1750, 60, "Manjar receta campo", "dulce,leche"],
    ["Legumbres Lentejas 400g", "Abarrotes", 1100, 1600, 50, "Lentejas seleccionadas", "lentils"],
    ["Garbanzos Wasil Caja 380g", "Abarrotes", 1000, 1450, 45, "Listos para servir", "chickpeas"],

    # DESAYUNO
    ["Café Nescafé Tradición 100g", "Desayuno", 2500, 3490, 80, "Café instantáneo clásico", "instant,coffee"],
    ["Café Fina Selección 170g", "Desayuno", 3800, 5290, 40, "Café tostado granulado", "coffee,jar"],
    ["Té Supremo Ceylan 100 bolsitas", "Desayuno", 1900, 2690, 120, "Té negro tradicional chileno", "tea,bags"],
    ["Té Club Premium 50 bolsitas", "Desayuno", 2100, 2890, 60, "Sabor intenso negro", "cup,tea"],
    ["Milo Nestlé Tarro 400g", "Desayuno", 2800, 3990, 50, "Bebida achocolatada", "chocolate,drink"],
    ["Avena Quaker Tradicional 500g", "Desayuno", 1200, 1690, 70, "Avena integral entera", "oats"],
    ["Cereal Chocapic Nestlé 400g", "Desayuno", 2500, 3600, 80, "Ondas de chocolate", "cereal,bowl"],
    ["Cereal Trix Nestlé Frutal 300g", "Desayuno", 2400, 3450, 50, "Figuras frutales infantiles", "cereal,colors"],
    ["Manjarate Soprole Pote", "Desayuno", 650, 950, 40, "Mousse tradicional", "mousse,dessert"],
    ["Mantequilla Colun Pan 250g", "Desayuno", 2200, 2990, 60, "Mantequilla del sur con sal", "butter"],
    ["Margarina Soprole Pote 500g", "Desayuno", 1300, 1890, 90, "Suave y untable", "margarine"],
    ["Mermelada Watt's Durazno 250g", "Desayuno", 900, 1290, 60, "Mermelada en vaso de vidrio", "jam,peach"],
    ["Mermelada Frutilla En Línea", "Desayuno", 1200, 1690, 40, "Sin azúcar añadida", "jam,strawberry"],
    ["Pan Integral Ideal Molde 500g", "Desayuno", 1800, 2400, 35, "Pan sano multicereal", "sliced,bread,wheat"],
    ["Pan Blanco Castaño Molde", "Desayuno", 1600, 2190, 45, "Blanco tradicional suave", "sliced,bread"],

    # PANADERÍA (Por KILO)
    ["Pan Marraqueta Blanca", "Panadería", 1300, 1990, 30, "Crujiente recién horneada", "bread,bakery"],
    ["Pan Hallulla Tradicional", "Panadería", 1300, 1990, 35, "Hallulla mantecosa especial", "round,bread"],
    ["Pan Coliza", "Panadería", 1350, 2100, 10, "Coliza tradicional campo", "bread"],
    ["Pan Integral", "Panadería", 1500, 2300, 15, "Alto en fibra", "whole,bread"],
    ["Dobladitas", "Panadería", 1600, 2400, 12, "Clásicas dobladitas chilensis", "pastry"],

    # LÁCTEOS
    ["Leche Colun Entera 1L", "Lácteos", 900, 1250, 120, "Leche de vaca del sur", "milk,carton"],
    ["Leche Soprole Descremada 1L", "Lácteos", 850, 1200, 100, "Leche baja en grasas", "milk,splash"],
    ["Yoghurt Soprole Batido Frutilla", "Lácteos", 250, 380, 200, "Yoghurt tradicional pote", "yogurt,strawberry"],
    ["Yoghurt Colun Vainilla Pote", "Lácteos", 280, 420, 150, "Cremosidad del sur", "yogurt"],
    ["Yoghurt Protein+ Soprole", "Lácteos", 550, 850, 80, "Alta proteína", "greek,yogurt"],
    ["Quesillo Colun Regular", "Lácteos", 1800, 2490, 40, "Fresco y natural", "white,cheese"],
    ["Leche Cultivada Soprole 1L", "Lácteos", 1300, 1850, 50, "Con probióticos", "fermented,milk"],
    ["Postre Chamyto Frutilla (Pack 6)", "Lácteos", 1400, 2100, 45, "Probióticos para la guata", "milk,drink"],
    ["Probiótico Uno Al Día", "Lácteos", 1600, 2300, 30, "Defensas activas", "yogurt,bottle"],
    ["Crema Soprole Caja 200ml", "Lácteos", 950, 1350, 80, "Crema líquida de leche", "cream,box"],

    # FIAMBRERÍA (Por KILO)
    ["Queso Gouda Soprole", "Fiambrería", 6500, 8990, 15, "Mantecoso estándar", "gouda,cheese"],
    ["Queso Chacra Llanquihue", "Fiambrería", 7000, 9500, 10, "Blanco tradicional", "fresh,cheese"],
    ["Jamón Pierna San Jorge", "Fiambrería", 5500, 7900, 20, "Jamón extra fino", "ham"],
    ["Jamón Pavo Receta del Abuelo", "Fiambrería", 7500, 9990, 12, "Para dietas ligeras", "turkey,ham"],
    ["Salame Ahumado PF", "Fiambrería", 8500, 11500, 8, "Corte delgado italiano", "salami"],
    ["Mortadela Lisa PF", "Fiambrería", 3500, 4990, 25, "Ideal para colaciones", "bologna"],
    ["Arrollado Huaso Cecinas Llanquihue", "Fiambrería", 8000, 10500, 10, "El real chileno, con ají", "pork,meat"],
    ["Pate de Ternera La Crianza (Pack)", "Fiambrería", 2200, 3100, 10, "Pate de campo importado", "pate"],
    ["Chorizo Parrillero San Jorge", "Fiambrería", 6000, 8500, 18, "Ideal asados", "chorizo"],
    ["Vienesa Tradicional PF", "Fiambrería", 2800, 3990, 35, "Para hot dogs y completos", "hotdog"],

    # CARNICERÍA (Por KILO)
    ["Lomo Vetado V", "Carnicería", 9500, 13990, 20, "El mejor para asados", "steak"],
    ["Posta Rosada Vacuno", "Carnicería", 7000, 9900, 25, "Ideal horno o cacerola", "beef,meat"],
    ["Huachalomo de Campo", "Carnicería", 6500, 8990, 30, "Para parrilladas económicas", "raw,meat"],
    ["Asado de Tira", "Carnicería", 8500, 11990, 15, "Sabor intenso con hueso", "ribs"],
    ["Pechuga Pollo Entera", "Carnicería", 3500, 4990, 40, "Pollo fresco sin marinar", "chicken,breast"],
    ["Trutro Entero Pollo Súper Pollo", "Carnicería", 2800, 4100, 35, "Frescos a granel", "chicken,leg"],
    ["Filete de Salmón Fresco", "Carnicería", 12000, 16500, 10, "Salmón del sur premium", "salmon"],
    ["Merluza Española Filet", "Carnicería", 4500, 6500, 15, "Para frituras", "fish,fillet"],
    ["Cerdo Chuleta Centro", "Carnicería", 4500, 6500, 20, "Corte magro y sabroso", "pork,chop"],
    ["Costillar Cerdo Aliñado", "Carnicería", 6000, 8500, 18, "Estilo chileno", "pork,ribs"],

    # BEBIDAS Y JUGOS
    ["Coca-Cola Original 3L", "Bebidas", 2300, 3100, 150, "Botella desechable", "coca,cola"],
    ["Coca-Cola Zero 3L", "Bebidas", 2300, 3100, 120, "Sin azúcar", "cola,drink"],
    ["Sprite 3L", "Bebidas", 2100, 2800, 80, "Sabor limón ultra fresca", "sprite,drink"],
    ["Fanta Naranja 3L", "Bebidas", 2100, 2800, 60, "Sabor naranja", "fanta,drink"],
    ["Kem Piña 1.5L", "Bebidas", 1200, 1650, 50, "Cultura chilena pop", "pineapple,soda"],
    ["Papaya Pap 1.5L", "Bebidas", 1200, 1650, 50, "Clásica papaya nacional", "papaya,soda"],
    ["Jugo Watt's Durazno 1.5L", "Bebidas", 1300, 1750, 90, "Jugo néctar botella", "peach,juice"],
    ["Jugo Andina del Valle Naranja 1.5L", "Bebidas", 1350, 1800, 70, "Bebida de jugo", "orange,juice"],
    ["Agua Mineral Cachantun Con Gas 1.6L", "Bebidas", 800, 1150, 120, "Agua mineral extraída en Chile", "sparkling,water"],
    ["Agua Vital Sin Gas 1.6L", "Bebidas", 750, 1050, 110, "Purificada embotellada", "water,bottle"],
    ["Cerveza Cristal Lata 473cc", "Bebidas", 600, 950, 200, "Cerveza nuestra", "beer,can"],
    ["Cerveza Escudo Lata 473cc", "Bebidas", 650, 1000, 150, "Más grado alcohólico", "beer"],
    ["Cerveza Royal Guard Lata (Pack 6)", "Bebidas", 4500, 5990, 30, "Premium nacional", "premium,beer"],
    ["Pisco Mistral 35 Grados 1L", "Bebidas", 6500, 8900, 20, "Añejado en roble", "pisco,bottle"],
    ["Vino Casillero del Diablo Carmenere", "Bebidas", 3500, 4800, 40, "Reserva del valle central", "red,wine"],
    
    # SNACKS Y CONFITES
    ["Papas Fritas Lays Clásicas 250g", "Snacks", 1800, 2400, 60, "Papas de corte liso saladas", "potato,chips"],
    ["Ramitas de Queso Evercrisp 200g", "Snacks", 1500, 2100, 50, "Las clásicas de cumpleaños", "cheese,snacks"],
    ["Doritos Queso 160g", "Snacks", 1600, 2200, 40, "Tortillas crocantes", "doritos"],
    ["Mani Salado Marco Polo 150g", "Snacks", 1100, 1500, 70, "Ideal pichangas", "peanuts"],
    ["Galletas Tritón Vainilla 126g", "Snacks", 600, 850, 100, "Negritas con relleno", "chocolate,cookies"],
    ["Galletas Tuareg Coco 130g", "Snacks", 650, 900, 80, "Bañadas estilo artesanal", "coco,cookies"],
    ["Galletas McKay Vino 150g", "Snacks", 500, 750, 120, "Clásicas para colación", "biscuit"],
    ["Galletas Frac Chocolate 130g", "Snacks", 700, 950, 90, "Doble chocolate", "oreo,cookie"],
    ["Super 8 Nestlé (Caja 24)", "Snacks", 4500, 6000, 15, "Obsequio clásico chileno", "wafer,chocolate"],
    ["Chocolate Costa Rama 115g", "Snacks", 1800, 2400, 30, "Hojuelas de chocolate", "chocolate,bar"],
    ["Chubi Nestlé Tubo Clásico", "Snacks", 400, 600, 100, "Confites de chocolate color", "candy,chocolates"],
    ["Gomitas Ambrosoli Osa 150g", "Snacks", 900, 1250, 60, "Sabores frutales surtidos", "gummy,bears"],
    ["Alfajor Lagos del Sur (Pack 6)", "Snacks", 1800, 2500, 25, "Bañados en torta", "alfajor"],
    ["Vizzio Costa Almendras 120g", "Snacks", 2200, 2900, 30, "Almendras bañadas 100%", "almond,chocolate"],
    ["Cheezels Queso Snack", "Snacks", 1100, 1500, 40, "Aros sabor queso maduro", "chips,ring"],

    # CONGELADOS
    ["Papas Prefritas Minuto Verde 1Kg", "Congelados", 2500, 3490, 40, "Listas para freír o horno", "french,fries"],
    ["Hamburguesas Receta Abuelo (Pack 5)", "Congelados", 4500, 6500, 20, "Burger gruesas 100g", "burger,patties"],
    ["Hamburguesas La Crianza (Pack 5)", "Congelados", 4000, 5800, 25, "Burger standard", "hamburger,frozen"],
    ["Helado Savory Trululu", "Congelados", 600, 900, 50, "Cara alegre de helado", "icecream,face"],
    ["Helado Bresler Almendrado", "Congelados", 900, 1300, 40, "Paleta bresler clásica", "icecream,stick"],
    ["Helado Savory Cassata 1L Piña", "Congelados", 2800, 3990, 15, "Pote familiar frutal", "icecream,tub"],
    ["Pizza Buitoni Pepperoni 400g", "Congelados", 4500, 6200, 20, "Pizza de piedra cruda", "frozen,pizza"],
    ["Choclo Desgranado Minuto Verde 500g", "Congelados", 1800, 2500, 30, "Choclo tierno granos", "corn,frozen"],
    ["Sofrito Base Congelado 400g", "Congelados", 1200, 1690, 35, "Cebolla tomate y ajo mix", "vegetables,mix"],
    ["Empanadas Queso Congeladas (10 un)", "Congelados", 3500, 4990, 15, "Formato cóctel frito rápido", "empanadas"],
    
    # ASEO LOCAL Y HOGAR
    ["Papel Higiénico Confort 40m (Pack 4)", "Aseo", 2800, 3900, 60, "Hoja simple", "toilet,paper"],
    ["Papel Higiénico Elite Doble Hoja (Pack 4)", "Aseo", 3500, 4800, 40, "Máxima suavidad", "toilet,roll"],
    ["Toalla Nova Clásica 2 Rollos", "Aseo", 1800, 2500, 80, "Para limpieza de cocina", "paper,towel"],
    ["Lavalozas Quix Limón 500ml", "Aseo", 1400, 1990, 100, "Desengrasante ultra", "dish,soap"],
    ["Detergente Omo Polvo 1Kg", "Aseo", 2500, 3600, 40, "Rendimiento familiar", "washing,powder"],
    ["Detergente Ariel Líquido 1.2L", "Aseo", 4500, 6200, 30, "Remueve manchas difíciles", "laundry,detergent"],
    ["Suavizante Fuzol Botella 1L", "Aseo", 1500, 2200, 50, "Cuidado suave ropa", "fabric,softener"],
    ["Cloro Tradicional Igenix 1L", "Aseo", 900, 1300, 120, "Cloro puro 5%", "bleach,bottle"],
    ["Limpiador Poett Lavanda 900ml", "Aseo", 1600, 2250, 70, "Pisos y baños frescos", "floor,cleaner"],
    ["Esponja Scotch-Brite (Pack 3)", "Aseo", 1800, 2500, 50, "La verde amarilla original", "sponge"],

    # CIUDADO PERSONAL
    ["Pasta Dental Pepsodent White 90g", "Perfumería", 1300, 1850, 100, "Protección blanqueadora", "toothpaste"],
    ["Pasta Dental Colgate Total 12 90g", "Perfumería", 1800, 2500, 80, "Multibeneficios sarro", "colgate,toothpaste"],
    ["Cepillo Dientes Oral-B Medio", "Perfumería", 1500, 2100, 60, "Cabezal estándar p/ adultos", "toothbrush"],
    ["Jabón Barra Ballerina Avena", "Perfumería", 600, 900, 150, "Humectante para manos", "soap,bar"],
    ["Jabón Líquido Dove 250ml", "Perfumería", 2500, 3500, 40, "Nutrición profunda pump", "liquid,soap"],
    ["Shampoo Ballerina Manzanilla 1L", "Perfumería", 2200, 3100, 60, "Extracto natural brillo", "shampoo,bottle"],
    ["Shampoo Head & Shoulders Clásico 400ml", "Perfumería", 3800, 5300, 45, "Control caspa", "shampoo"],
    ["Acondicionador Sedal Ceramidas 340ml", "Perfumería", 2600, 3600, 35, "Brillo extremo", "conditioner"],
    ["Desodorante Spray Rexona Hombre", "Perfumería", 2800, 3990, 50, "Motion sense", "deodorant,spray"],
    ["Desodorante Roll-On Dove Original", "Perfumería", 2200, 3100, 40, "Cuidado diario sin alcohol", "deodorant,rollon"],

    # MISCELLANEA EXTRA
    ["Pilas Energizer AA (Pack 4)", "Abarrotes", 3500, 4990, 50, "Alcalinas largo uso", "aa,batteries"],
    ["Pilas Duracell AAA (Pack 2)", "Abarrotes", 2800, 3900, 60, "Duracell power plus", "aaa,batteries"],
    ["Fósforos Copihue Caja Grande", "Abarrotes", 300, 500, 150, "Para cocinas a gas", "matches,box"],
    ["Carbón Vegetal Asados 2Kg", "Feria", 2500, 3800, 30, "Parrillas y chimeneas chispa", "charcoal"],
    ["Leña Saco Eucaliptus", "Feria", 4000, 6000, 20, "Leña seca certificada", "firewood"],
    ["Alimento Perro Pedigree 3Kg", "Abarrotes", 6500, 8990, 25, "Nutrición canes adultos", "dog,food"],
    ["Alimento Gato Whiskas 3Kg", "Abarrotes", 7000, 9500, 20, "Sabor pescado mix", "cat,food"],
    ["Arena Sanitaria Gatos 4Kg", "Abarrotes", 3500, 4990, 30, "Aglutinante libre olores", "cat,litter"],
    ["Repelente Mosquitos Raid 400ml", "Aseo", 3800, 5200, 15, "Mata insectos al paso", "bug,spray"],
    ["Velas Blancas (Pack 6)", "Abarrotes", 900, 1300, 80, "Apagones zona central", "candles"]
]

# Totalizamos 115 items aquí, duplicaremos variantes pequeñas para llegar a 150 exactamente
# Vamos a inyectar las 35 restantes dinámicamente con un loop para bebidas, cervezas y golosinas extras:

extras = []
sabores_jugo = ["Manzana", "Piña", "Frutilla", "Mango", "Tutti Frutti"]
for s in sabores_jugo:
    nombre = f"Néctar Andina del Valle {s} 1.5L"
    extras.append([nombre, "Bebidas", 1350, 1800, 50, f"Jugo de {s} fresco", f"{s.lower()},juice"])
    nombre2 = f"Sobre Jugo Zuko {s}"
    extras.append([nombre2, "Desayuno", 200, 350, 200, f"Rinde 1 litro de {s}", "powder,juice"])
    nombre3 = f"Sobre Jugo Livean {s}"
    extras.append([nombre3, "Desayuno", 250, 400, 180, f"Sin calorías de {s}", "diet,beverage"])

# 15 marcas confites chilenos
confites = ["Masticable Media Hora", "Serranita McKay", "Carioca McKay", "Kuky McKay", "Rollo Selva", "Chicles Dos en Uno", "Gomitas Frugelé", "Bombones Garoto", "Tableta Trencito", "Maní Confitado Pote", "Chocolinas", "Caramelos Anís", "Koyak Surtido", "Blenox", "Mentitas Ambrosoli"]
for idx, c in enumerate(confites):
    extras.append([c, "Snacks", 300, 500 + (idx*50), 60, "Golosinas tradicionales kiosko", "candy"])


# Sumamos todo
todos = productos + extras

# Asegurar exactitud (son 115 + 15 + 15 + 5 = 150)
if len(todos) > 150:
    todos = todos[:150]

csv_content = "ID,NOMBRE,CATEGORIA,PRECIO_COMPRA,PRECIO_VENTA,STOCK,DESCRIPCION,URL_IMAGEN\n"
for idx, p in enumerate(todos, 1):
    unsplash_url = f"https://loremflickr.com/320/240/{p[6]}?random={idx}"
    csv_content += f"{idx},{p[0]},{p[1]},{p[2]},{p[3]},{p[4]},{p[5]},{unsplash_url}\n"

# Paths
desktop_path_1 = "C:\\Users\\gomez\\OneDrive\\Desktop\\MARKET GOMEZ\\catalogo_limber_150.csv"

# Resolving universal Desktop via env (usually C:\Users\user\Desktop or C:\Users\user\OneDrive\Desktop)
import ctypes.wintypes
CSIDL_DESKTOP = 0
buf = ctypes.create_unicode_buffer(ctypes.wintypes.MAX_PATH)
ctypes.windll.shell32.SHGetFolderPathW(None, CSIDL_DESKTOP, None, 0, buf)
desktop_path_2 = os.path.join(buf.value, "catalogo_limber_150.csv")

try:
    with open(desktop_path_1, 'w', encoding='utf-8') as f1:
        f1.write(csv_content)
    print(f"✅ Guardado en: {desktop_path_1}")
except Exception as e:
    print(f"Error {desktop_path_1}: {e}")

try:
    with open(desktop_path_2, 'w', encoding='utf-8') as f2:
        f2.write(csv_content)
    print(f"✅ Guardado en: {desktop_path_2}")
except Exception as e:
    # Fallback to direct C:\Users\gomez\Desktop fallback
    desktop_path_3 = "C:\\Users\\gomez\\Desktop\\catalogo_limber_150.csv"
    try:
        with open(desktop_path_3, 'w', encoding='utf-8') as f3:
           f3.write(csv_content)
        print(f"✅ Guardado en: {desktop_path_3}")
    except:
        pass
