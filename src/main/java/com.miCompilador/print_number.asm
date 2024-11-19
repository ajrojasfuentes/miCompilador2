print_number:
    ; Asumimos que el número está en eax
    ; Convertir el número a cadena y llamar a write
    push eax
    mov ecx, buffer
    add ecx, 11      ; Ubicación del terminador de cadena
    mov byte [ecx], 0 ; Terminador nulo

    mov ebx, 10
convert_loop:
    mov edx, 0
    div ebx
    add edx, '0'
    dec ecx
    mov [ecx], dl
    cmp eax, 0
    jne convert_loop

    ; Llamar a write para imprimir la cadena
    mov eax, 4       ; sys_write
    mov ebx, 1       ; stdout
    mov edx, 11
    sub edx, ecx
    mov ecx, ecx
    int 0x80

    pop eax
    ret

section .bss
buffer resb 12      ; Buffer para la cadena (incluye terminador)
