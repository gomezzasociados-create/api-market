class MarvitAI {
    constructor() {
        this.synth = window.speechSynthesis;
        this.voice = null;
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        
        // Reconocimiento de voz
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (SpeechRecognition) {
            this.recognition = new SpeechRecognition();
            this.recognition.lang = 'es-ES';
            this.recognition.continuous = false; // Por defecto corto
            this.recognition.interimResults = false;
        } else {
            console.warn("Speech Recognition no soportado en este navegador.");
            this.recognition = null;
        }
        
        this.isListening = false;
        
        // Esperemos a que carguen las voces
        if (this.synth && this.synth.onvoiceschanged !== undefined) {
            this.synth.onvoiceschanged = () => this.loadVoice();
        }
    }

    loadVoice() {
        if(this.voice) return;
        const voices = this.synth.getVoices();
        // Buscamos una voz en español, preferiblemente masculina/robótica u offline nativa
        this.voice = voices.find(v => v.lang.startsWith('es') && (v.name.includes('Google') || v.name.includes('Microsoft'))) || voices.find(v => v.lang.startsWith('es')) || voices[0];
    }

    speak(text) {
        if (!this.synth) return;
        if (this.synth.speaking) {
            // this.synth.cancel(); // Si queremos que interrumpa
        }
        const utterThis = new window.SpeechSynthesisUtterance(text);
        if (this.voice) utterThis.voice = this.voice;
        
        utterThis.pitch = 0.8; // Más profundo
        utterThis.rate = 1.1;  // Un poco más rápido
        
        this.synth.speak(utterThis);
    }

    _oscillator(type, freq, vol, duration, slideTo = null) {
        if (!this.audioCtx) return;
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume().catch(e => console.warn("Audio ignorado por navegador: ", e));
        }
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        
        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime);
        if (slideTo) {
            osc.frequency.exponentialRampToValueAtTime(slideTo, this.audioCtx.currentTime + duration);
        }
        
        gain.gain.setValueAtTime(vol, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + duration);
        
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        
        osc.start();
        osc.stop(this.audioCtx.currentTime + duration);
    }

    playBoot() {
        this._oscillator('sine', 100, 0.5, 1.5, 800);
        setTimeout(() => this._oscillator('square', 800, 0.1, 0.3), 1000);
    }

    playScan() {
        this._oscillator('sine', 1200, 0.1, 0.1);
    }

    playClick() {
        this._oscillator('square', 600, 0.05, 0.05);
    }

    playError() {
        this._oscillator('sawtooth', 150, 0.3, 0.4);
        setTimeout(() => this._oscillator('sawtooth', 100, 0.3, 0.4), 150);
    }

    playSuccess() {
        this._oscillator('sine', 600, 0.2, 0.1);
        setTimeout(() => this._oscillator('sine', 1200, 0.2, 0.2), 100);
    }

    // continuousMode true para dictado largo
    startListening(onResultCallback, continuousMode = false) {
        if (!this.recognition) return;
        
        if (continuousMode) {
            this.recognition.continuous = true;
            this.recognition.interimResults = true; // Permite ver lo que escribe
        } else {
            this.recognition.continuous = false;
            this.recognition.interimResults = false;
        }

        this.recognition.onstart = () => {
            this.isListening = true;
            this._oscillator('sine', 2000, 0.05, 0.2); // Sonido de inicio escucha
            const ui = document.getElementById('marvit-core-ui');
            if(ui) ui.classList.add('listening-active');
        };

        this.recognition.onresult = (event) => {
            let final_transcript = '';
            let interim_transcript = '';

            for (let i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    final_transcript += event.results[i][0].transcript;
                } else {
                    interim_transcript += event.results[i][0].transcript;
                }
            }
            onResultCallback(final_transcript, interim_transcript);
        };

        this.recognition.onerror = (e) => {
            console.error(e);
            this.stopListening();
        };

        this.recognition.onend = () => {
            this.isListening = false;
            const ui = document.getElementById('marvit-core-ui');
            if(ui) ui.classList.remove('listening-active');
            
            // Si estaba escribiendo largo, damos un ping de cierre
            if(continuousMode) this._oscillator('sine', 1800, 0.05, 0.2, 1200);
        };

        try {
            this.recognition.start();
        } catch(e) {}
    }

    stopListening() {
        if(this.recognition && this.isListening) {
            this.recognition.stop();
        }
    }

    injectUI() {
        const div = document.createElement('div');
        div.id = 'marvit-core-ui';
        div.innerHTML = `
            <style>
                #marvit-core-ui {
                    position: fixed;
                    bottom: 30px;
                    right: 30px;
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    background: radial-gradient(circle, rgba(0,210,255,1) 0%, rgba(30,10,50,1) 100%);
                    box-shadow: 0 0 20px rgba(0, 210, 255, 0.5), inset 0 0 20px rgba(255,255,255,0.5);
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    z-index: 10000;
                    transition: all 0.3s;
                    border: 2px solid rgba(0, 210, 255, 0.8);
                }
                #marvit-core-ui:hover {
                    box-shadow: 0 0 40px rgba(0, 210, 255, 0.9);
                    transform: scale(1.1);
                }
                #marvit-core-ui.listening-active {
                    animation: marvitPulse 1s infinite alternate;
                    background: radial-gradient(circle, rgba(255,42,95,1) 0%, rgba(30,10,50,1) 100%);
                    box-shadow: 0 0 30px rgba(255, 42, 95, 0.8);
                    border-color: rgba(255,42,95, 1);
                }
                #marvit-core-ui::after {
                    content: '🔊';
                    font-size: 24px;
                    opacity: 0.8;
                }
                #marvit-core-ui.listening-active::after {
                    content: '🎙️';
                }
                @keyframes marvitPulse {
                    from { transform: scale(1); box-shadow: 0 0 20px rgba(255,42,95,0.5); }
                    to { transform: scale(1.15); box-shadow: 0 0 40px rgba(255,42,95,1); }
                }
            </style>
        `;
        document.body.appendChild(div);

        // Control simple: Click normal escucha corto. Click largo / Doble click activa constante.
        // Simularemos dictado continuo con presionar y mantener, o click un toggle para dictar
        let isContinuous = false;
        
        div.addEventListener('click', () => {
            if(this.isListening) {
                this.stopListening();
                this.speak("Conexión de micrófono finalizada por G.O.M.E.Z.");
            } else {
                // Check if shift is held for dictation
                const wantsDictation = window.event && window.event.shiftKey;
                if(wantsDictation) {
                    this.speak("Modo de transcripción de texto G.O.M.E.Z. activado.");
                    this.startListening((f, i) => {
                        // Despachar evento global para que el input focalizado lo intercepte
                        window.dispatchEvent(new CustomEvent('marvitDictation', { detail: { isFinal: f!== "", text: f || i } }));
                    }, true);
                } else {
                    this.playScan();
                    this.startListening((f, i) => {
                        window.dispatchEvent(new CustomEvent('marvitCommand', { detail: { text: f } }));
                    }, false);
                }
            }
        });
        
        div.title = "Click para dar orden a G.O.M.E.Z. |  Shift+Click para escanear voces en textos largos.";
    }
}

const MARVIT = new MarvitAI();
window.addEventListener('load', () => {
    MARVIT.injectUI();
});
