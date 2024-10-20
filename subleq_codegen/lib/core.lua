z  		  	= word(0)
n1 		  	= word(-1)
p1 		  	= word(1)
p2 		  	= word(2)
p3 		  	= word(3)
p4 		  	= word(4)
p10 	  	= word(10)
p32 	  	= word(32)


t0 			= word()
t1 			= word()
t2 			= word()
t3 			= word()
t4 			= word()
t5 			= word()
t6 			= word()
t7 			= word()
t8 			= word()
t9 			= word()
tA 			= word()


function sub(a, b)
    sble(a, b) -- b -= a
end

function add(a, b)
    sub(a, z) -- z -= a
    sub(z, b) -- b -= z
    sub(z, z) -- z -= z
end

function jump(addr)
    sble(z, z, addr)
end

function clear(x)
    sble(x, x) -- x -= x
end

function move(src, dest)
    clear(dest)    -- dest -= dest
    add(src, dest) -- dest += src
end

function inc(n)
    sble(n1, n) -- n -= n1 (-1)
end

function dec(n)
    sble(p1, n) -- n -= p1 (1)
end

function jgez(n, addr)
    local gte = label()
    sble(n, z, gte)
    local done = label()
    jump(done)
    mark(gte)
    sble(z, z, addr)
    mark(done)
    sble(z, z)
end

function jnz(n, addr)
    local lte = label()
    sble(z, n, lte)
    jump(addr)
    mark(lte)
    local gte = label()
    sble(n, z, gte)
    jump(addr)
    mark(gte)
    clear(z)
end

function jz(n, addr)
    local lte = label()
    sble(z, n, lte)
    local done = label()
    sble(z, z, done)
    mark(lte)
    local gte = label()
    sble(n, z, gte)
    sble(z, z, done)
    mark(gte)
    sble(z, z, addr)
    mark(done)
end

function jgz(n, addr)
    local skip = label()
    jz(n, skip)
    sble(z, n, skip)
    jump(addr)
    mark(skip)
end

function halt()
    sble(-1,-1,-1)
end

function ldref(addr, dest)
    clear(dest)
    move(addr, at(pos() + 12))
    sble(0, z)
    sble(z, dest)
    clear(z)
end

function stref(src, addr)
    move(addr, at(pos() + 24))
    move(addr, at(pos() + 25))
    sble(0, 0)
    move(addr, at(pos() + 16))
    sble(src, z)
    sble(z, 0)
    clear(z)
end

function divmod(q, r, a, b)
    clear(q)
    clear(r)
    clear(t0)
    clear(t1)
    move(a, t1)
    local loop = mark()
    move(t1, t0)
    sub(b, t0)
    local skip = label()
    jgez(t0, skip)
    local lend = label()
    jump(lend)
    mark(skip)
        sub(b, t1)
        inc(q)
    jump(loop)
    mark(lend)
    move(t1, r)
end