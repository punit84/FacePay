import re

class AcronymPhonemeConverter:
    def __init__(self):
        self.acronym_ipa_map = {
            "RBI": "ˌɑːr.biːˈaɪ",
            "NPCI": "ˌɛn.piː.siːˈaɪ",
            "UPI": "ˈjuː.piː.aɪ",
            "NEFT": "ˈɛn.iː.ɛf.tiː",
            "IMPS": "ˌaɪ.ɛm.piː.ɛs",
            "ATM": "ˌeɪ.tiːˈɛm",
            "PIDF": "ˈpiː.aɪ.diː.ɛf",
            "RuPay": "ˈruː.peɪ",
            "MTU": "ˌɛm.tiːˈjuː",
            "PPSL": "ˌpiː.piː.ɛsˈɛl",
            "Paytm": "ˈpeɪ.tiː.ɛm",
            "EBITDA": "ˈiː.bɪt.dɑː",
            "mutabik": "muːˈt̪aː.bɪk",
            "QoQ": "ˌkjuː.əʊˈkjuː",
            "MoM": "ˌɛm.əʊˈɛm",
            "YoY": "ˌwaɪ.əʊˈwaɪ",
            "ID": "ˌaɪˈdiː",
            "sakti": "səˈk.t̪i",
            "janakar": "ˈd͡ʒaː.nə.kər",
            "tariikh": "t̪aːˈriːx"
        }

        # Precompile regex pattern
        acronyms = sorted(self.acronym_ipa_map.keys(), key=lambda x: -len(x))  # longest first to avoid partial match
        acronym_pattern = '|'.join(map(re.escape, acronyms))
        self.pattern = re.compile(rf'(?<!\w)({acronym_pattern})([\u0900-\u097F]*)', flags=re.UNICODE)

    def convert_to_ssml(self, text: str) -> str:
        def replace(match):
            acronym = match.group(1)
            suffix = match.group(2) or ''
            ipa = self.acronym_ipa_map[acronym]
            return f'<phoneme alphabet="ipa" ph="{ipa}">{acronym}</phoneme>{suffix}'

        converted = self.pattern.sub(replace, text)
        return f"<speak>{converted}</speak>"