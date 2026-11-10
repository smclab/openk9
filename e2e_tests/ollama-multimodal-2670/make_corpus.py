"""
Disegna il corpus dello scenario con Pillow e lo stampa su stdout come JSON
{nome file: base64}. Gira nel container del modulo di embedding, che ha
Pillow (run.sh: docker exec -i ... python - < make_corpus.py).

  bilge.jpg     pagina "Bilge pump diagram": scafo, pozzetto, pompa, tubi
  cabin.jpg     pagina "Cabin interior layout": pianta degli interni
  manual.pdf    tre pagine: impianto elettrico, sentina, interni
  query.jpg     ritaglio dello schema della sentina, ruotato e scalato come
                una foto fatta a bordo
"""
import base64
import io
import json

from PIL import Image, ImageDraw, ImageFont

W, H = 800, 1000
BLACK, BLUE, GREY = (20, 20, 20), (30, 70, 160), (120, 120, 120)


def font(size):
    return ImageFont.load_default(size=size)


def page(title):
    image = Image.new("RGB", (W, H), "white")
    draw = ImageDraw.Draw(image)
    draw.text((60, 50), title, fill=BLACK, font=font(44))
    draw.line((60, 115, W - 60, 115), fill=GREY, width=3)
    return image, draw


def label(draw, xy, text, size=22):
    draw.text(xy, text, fill=BLACK, font=font(size))


def arrow(draw, start, end):
    draw.line((start, end), fill=BLUE, width=6)
    x, y = end
    sx, sy = start
    if abs(x - sx) > abs(y - sy):
        d = 1 if x > sx else -1
        draw.polygon([(x, y), (x - 18 * d, y - 10), (x - 18 * d, y + 10)], fill=BLUE)
    else:
        d = 1 if y > sy else -1
        draw.polygon([(x, y), (x - 10, y - 18 * d), (x + 10, y - 18 * d)], fill=BLUE)


def bilge():
    image, draw = page("Bilge pump diagram")
    # scafo in sezione e pozzetto di sentina
    draw.line([(80, 300), (160, 780), (400, 860), (640, 780), (720, 300)], fill=BLACK, width=6)
    draw.rectangle((330, 760, 470, 850), outline=BLACK, width=4)
    draw.rectangle((340, 800, 460, 845), fill=(150, 190, 230))
    label(draw, (300, 870), "BILGE WELL")
    # pompa e interruttore a galleggiante
    draw.ellipse((360, 560, 440, 640), outline=BLACK, width=5)
    label(draw, (388, 584), "P", 30)
    label(draw, (460, 585), "BILGE PUMP")
    draw.rectangle((250, 790, 300, 830), outline=BLACK, width=4)
    label(draw, (150, 740), "FLOAT SWITCH")
    # aspirazione dal pozzetto e mandata fuori bordo
    arrow(draw, (400, 790), (400, 645))
    draw.line((400, 560, 400, 400), fill=BLUE, width=6)
    arrow(draw, (400, 400), (700, 400))
    draw.ellipse((700, 385, 730, 415), outline=BLACK, width=4)
    label(draw, (600, 430), "OVERBOARD")
    label(draw, (60, 940), "Fig. 7 - bilge system: well, pump, discharge", 20)
    return image


def cabin():
    image, draw = page("Cabin interior layout")
    draw.rectangle((80, 170, 720, 900), outline=BLACK, width=6)
    rooms = [
        ((80, 170, 720, 330), "V-BERTH"),
        ((80, 330, 400, 600), "SALON"),
        ((400, 330, 720, 470), "GALLEY"),
        ((400, 470, 720, 600), "HEAD"),
        ((80, 600, 400, 900), "AFT CABIN"),
        ((400, 600, 720, 900), "NAV STATION"),
    ]
    for box, name in rooms:
        draw.rectangle(box, outline=BLACK, width=3)
        label(draw, (box[0] + 20, box[1] + 20), name)
    draw.ellipse((170, 430, 310, 530), outline=BLACK, width=4)
    draw.rectangle((120, 680, 360, 860), outline=GREY, width=3)
    draw.rectangle((450, 680, 650, 760), outline=GREY, width=3)
    label(draw, (60, 940), "Fig. 3 - interior: berths, salon, galley", 20)
    return image


def electrical():
    image, draw = page("Electrical panel wiring")
    for i in range(2):
        draw.rectangle((100 + i * 180, 700, 240 + i * 180, 820), outline=BLACK, width=4)
        label(draw, (120 + i * 180, 740), "12V BATT")
    draw.rectangle((420, 200, 720, 600), outline=BLACK, width=5)
    for row in range(6):
        for col in range(2):
            x, y = 450 + col * 130, 230 + row * 60
            draw.rectangle((x, y, x + 100, y + 40), outline=BLACK, width=3)
    label(draw, (440, 610), "BREAKER PANEL")
    for i, color in enumerate(((200, 30, 30), (30, 30, 30))):
        draw.line((170 + i * 180, 700, 170 + i * 180, 640 - i * 30, 420, 640 - i * 30), fill=color, width=6)
    label(draw, (60, 940), "Fig. 9 - house batteries and breakers", 20)
    return image


def encode(image, fmt, **kwargs):
    out = io.BytesIO()
    image.save(out, fmt, **kwargs)
    return base64.b64encode(out.getvalue()).decode()


def main():
    bilge_page, cabin_page = bilge(), cabin()
    query = (
        bilge_page.crop((60, 280, 760, 920))
        .rotate(4, expand=True, fillcolor="white")
        .resize((520, 490))
    )
    print(json.dumps({
        "bilge.jpg": encode(bilge_page, "JPEG", quality=90),
        "cabin.jpg": encode(cabin_page, "JPEG", quality=90),
        "manual.pdf": encode(electrical(), "PDF", save_all=True,
                             append_images=[bilge_page, cabin_page], resolution=100),
        "query.jpg": encode(query, "JPEG", quality=85),
    }))


main()
