local start = label()
sble(0, 0, start)


include "lib/core.lua"
include "lib/io.lua"


local msg               = asciiz("Hello, World!\n")
local ptr_orig          = word(msg.addr)
local ptr               = word(msg.addr)

local x                 = word(0)

                        mark(start)

local loop = mark()     ldref(ptr, x)
local lend = label()    jz(x, lend)
                        putc(x)
                        inc(ptr)
                        jump(loop)
                        mark(lend)

                        move(ptr_orig, ptr)
                        clear(x)
                        jump(start)



--local hang = mark()
--jump(hang)