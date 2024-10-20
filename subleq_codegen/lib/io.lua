include "lib/core.lua"


PUTC_ADDR   = -1


pascii0     = word(string.byte('0'))
pasciineg   = word(string.byte('-'))
p10_10k     = mark() word(10) word(100) word(1000) word(10000) -- 000A, 0064, 03E8, 2710
_p10_10k    = word(p10_10k.addr)


function putc(x)
    sble(x, z)
    sble(z, PUTC_ADDR)
    clear(z)
end

function putln()
    putc(p10)
end

function putspace()
    putc(p32)
end

function putudec(x)
    move(x, t2)
    move(p3, t3)
    clear(t4)
    local loop = mark()
        move(_p10_10k, t5)
        add(t3, t5)
        ldref(t5, t6)

        move(t2, t7)
        sub(t6, t7)
        local sk0 = label()
        jgez(t7, sk0)
        local sk1 = label()
            
            jz(t4, sk1)
            putc(pascii0)

        jump(sk1)
        mark(sk0)

            move(p1, t4)

            divmod(t8, t9, t2, t6)
            add(pascii0, t8)
            putc(t8)
            move(t9, t2)

        mark(sk1)

        dec(t3)
        jgez(t3, loop)

    local sk2 = label()
    jz(t4, sk2)
    jgez(t2, sk2)
    local sk3 = label()
    jump(sk3)
    mark(sk2)

        add(pascii0, t2)
        putc(t2)

    mark(sk3)
end

function putdec(x)
    local l1 = label()
    local l2 = label()
    jgez(x, l1)
        putc(pasciineg)
        clear(tA)
        sub(x, tA)
    jump(l2)
    mark(l1)
        move(x, tA)
    mark(l2)
    putudec(tA)
end