"""Generate the PrayerReminder voice clips (Urdu, Arabic, English).

This is the script that made the PREVIEW pack in design/audio/voice-pack-preview.
The sentences, voices and file names here are the source of truth for the final clips.

Clip naming: <kind>_<prayer>[_<minutes>].mp3
  kind: before_azan | at_azan | before_jamaat
  prayer: fajr | dhuhr | asr | maghrib | isha | jumuah
"""
import asyncio, os, sys, json
import edge_tts  # preview generator. For RELEASE clips use Azure AI Speech with the same voice names (see spec 11.3)

OUT = sys.argv[1] if len(sys.argv) > 1 else 'pack'
PRAYERS = ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha', 'jumuah']
BEFORE_AZAN = [5, 10, 15, 20, 30]
BEFORE_JAMAAT = [2, 5, 10, 15]

VOICES = {
    'ur': {'male': 'ur-IN-SalmanNeural', 'female': 'ur-IN-GulNeural'},
    'ar': {'male': 'ar-SA-HamedNeural', 'female': 'ar-SA-ZariyahNeural'},
    'en': {'male': 'en-IN-PrabhatNeural', 'female': 'en-IN-NeerjaNeural'},
}

# ---- Urdu ----
UR_N = {2: 'دو', 5: 'پانچ', 10: 'دس', 15: 'پندرہ', 20: 'بیس', 30: 'تیس'}
UR_P = {'fajr': 'فجر', 'dhuhr': 'ظہر', 'asr': 'عصر', 'maghrib': 'مغرب', 'isha': 'عشاء', 'jumuah': 'جمعہ'}
def ur(kind, p, n=None):
    if kind == 'before_azan':   return f'{UR_P[p]} کی اذان میں {UR_N[n]} منٹ باقی ہیں'
    if kind == 'at_azan':       return f'{UR_P[p]} کی اذان کا وقت ہو گیا ہے'
    return f'{UR_P[p]} کی نماز شروع ہونے میں {UR_N[n]} منٹ باقی ہیں'

# ---- Arabic ----
AR_N = {2: 'دقيقتان', 5: 'خمسُ دقائق', 10: 'عشرُ دقائق', 15: 'خمسَ عشرةَ دقيقة', 20: 'عشرون دقيقة', 30: 'ثلاثون دقيقة'}
AR_P = {'fajr': 'الفجر', 'dhuhr': 'الظهر', 'asr': 'العصر', 'maghrib': 'المغرب', 'isha': 'العشاء', 'jumuah': 'الجمعة'}
def ar(kind, p, n=None):
    if kind == 'before_azan':   return f'بقيت {AR_N[n]} على أذان {AR_P[p]}'
    if kind == 'at_azan':       return f'حان الآن موعد أذان {AR_P[p]}'
    return f'بقيت {AR_N[n]} على إقامة صلاة {AR_P[p]}'

# ---- English (spelled the way the names are said in India, so the voice pronounces them well) ----
EN_N = {2: 'Two', 5: 'Five', 10: 'Ten', 15: 'Fifteen', 20: 'Twenty', 30: 'Thirty'}
EN_P = {'fajr': 'Fajr', 'dhuhr': 'Zohar', 'asr': 'Asar', 'maghrib': 'Maghrib', 'isha': 'Isha', 'jumuah': 'Jumma'}
def en(kind, p, n=None):
    if kind == 'before_azan':   return f'{EN_N[n]} minutes left for the {EN_P[p]} azaan.'
    if kind == 'at_azan':       return f'It is time for the {EN_P[p]} azaan.'
    return f'{EN_N[n]} minutes left for {EN_P[p]} prayer to start.'

TEXT = {'ur': ur, 'ar': ar, 'en': en}

def jobs():
    for lang in ['ur', 'ar', 'en']:
        for p in PRAYERS:
            for n in BEFORE_AZAN:
                yield lang, 'male', f'before_azan_{p}_{n}', TEXT[lang]('before_azan', p, n)
            yield lang, 'male', f'at_azan_{p}', TEXT[lang]('at_azan', p)
            for n in BEFORE_JAMAAT:
                yield lang, 'male', f'before_jamaat_{p}_{n}', TEXT[lang]('before_jamaat', p, n)

async def one(sem, lang, gender, name, text, folder):
    path = os.path.join(folder, f'{name}.mp3')
    if os.path.exists(path) and os.path.getsize(path) > 1000:
        return path, text
    async with sem:
        for attempt in range(4):
            try:
                await edge_tts.Communicate(text, VOICES[lang][gender], rate='-8%').save(path)
                if os.path.getsize(path) > 1000:
                    return path, text
            except Exception as e:
                await asyncio.sleep(1 + attempt)
        raise RuntimeError(f'failed {path}')

async def main():
    sem = asyncio.Semaphore(6)
    tasks, manifest = [], {}
    for lang, gender, name, text in jobs():
        folder = os.path.join(OUT, 'voice-pack-preview', lang); os.makedirs(folder, exist_ok=True)
        tasks.append(one(sem, lang, gender, name, text, folder))
        manifest.setdefault(lang, {})[name] = text
    # female samples (one sentence each) so the owner can compare voices
    sfolder = os.path.join(OUT, 'female-voice-samples'); os.makedirs(sfolder, exist_ok=True)
    for lang in ['ur', 'ar', 'en']:
        tasks.append(one(sem, lang, 'female', f'female_{lang}_before_azan_asr_10', TEXT[lang]('before_azan', 'asr', 10), sfolder))
    res = await asyncio.gather(*tasks)
    with open(os.path.join(OUT, 'voice-pack-preview', 'phrases.json'), 'w', encoding='utf-8') as f:
        json.dump({'voices': VOICES, 'before_azan_minutes': BEFORE_AZAN, 'before_jamaat_minutes': BEFORE_JAMAAT,
                   'prayers': PRAYERS, 'phrases': manifest}, f, ensure_ascii=False, indent=2)
    print('clips:', len(res))

asyncio.run(main())
