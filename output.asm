section .data
section .bss
a resd 1
section .text
global _start
_start:
    ; Asignación
    mov eax, 0
    mov [a], eax
    ; Salir del programa
    mov eax, 1
    mov ebx, 0
    int 0x80

; Funciones auxiliares
print_number:
    ; Implementación de print_number
    ret

