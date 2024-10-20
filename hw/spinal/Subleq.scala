import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class SubleqConfig(
    val width: Int = 16
) {
    def dtype() = SInt(width bits)
    def reg() = Reg(dtype())
}


case class SubleqBus(cfg: SubleqConfig) extends Bundle {
    val addr = out(cfg.dtype())
    val rdata = in(cfg.dtype())
    val wdata = out(cfg.dtype())
    val write = out(Bool())
}


case class Subleq(cfg: SubleqConfig) extends Component {
    val io = new Bundle {
        val bus = SubleqBus(cfg)
    }

    val regs = new Area {
        val a = cfg.reg()
        val b = cfg.reg()
        val c = cfg.reg()
        val ip = cfg.reg() init(0)
    }

    val b_addr = cfg.reg()
    val b_wdata = cfg.reg()
    val b_write = Reg(Bool()) init(False)

    io.bus.addr := b_addr

    when(b_write) {
        io.bus.wdata := b_wdata
    } otherwise {
        io.bus.wdata := 0
    }

    io.bus.write := b_write

    val t0 = cfg.reg()
    val t1 = cfg.reg()

    val nIp = regs.ip + 1

    val sub = t1 - t0
    val br = sub <= 0

    val state = Reg(UInt(4 bits)) init(0)

    switch(state) {
        is(0) {
            b_addr := regs.ip
        }
        is(1) {
            regs.a := io.bus.rdata
            regs.ip := nIp
        }

        is(2) {
            b_addr := regs.ip
        }
        is(3) {
            regs.b := io.bus.rdata
            regs.ip := nIp
        }
        
        is(4) {
            b_addr := regs.ip
        }
        is(5) {
            regs.c := io.bus.rdata
            regs.ip := nIp
        }

        is(6) {
            b_addr := regs.a
        }
        is(7) {
            t0 := io.bus.rdata
        }

        is(8) {
            b_addr := regs.b
        }
        is(9) {
            t1 := io.bus.rdata            
        }

        is(10) {
            b_addr := regs.b
            b_wdata := sub
            b_write := True
        }
        is(11) {
            b_write := False
        }

        is(12) {
            when(br) {
                regs.ip := regs.c
            }
        }
    }

    when(state === 12) {
        state := 0
    } otherwise {
        state := state + 1
    }

    /*
    val fsm = new StateMachine {
        def fetchRegState(addr: SInt, reg: SInt) = new State {
            whenIsActive {
                b_addr := addr
                b_write := False
            }
            onExit {
                regs.ip := nIp
            }
        }

        val fetchA = fetchRegState(regs.ip, regs.a)
        setEntry(fetchA)

        val fetchB = fetchRegState(regs.ip, regs.b)
        fetchA.whenIsActive(goto(fetchB))

        val fetchC = fetchRegState(regs.ip, regs.c)
        fetchB.whenIsActive(goto(fetchC))

        val fetchT0 = fetchRegState(regs.a, t0)
        fetchC.whenIsActive(goto(fetchT0))

        val fetchT1 = fetchRegState(regs.b, t1)
        fetchT0.whenIsActive(goto(fetchT1))

        val execute = new State {
            whenIsActive {
                b_addr := regs.b
                b_wdata := r
                b_write := True
                goto(fetchA)
            }
            onExit {
                when(br) {
                    regs.ip := regs.c
                } otherwise {
                    regs.ip := nIp
                }
            }
        }
        fetchT1.whenIsActive(goto(execute))
    }
    */
}

/*

void run_subleq(void) {
    s16 a, b, c, r;
    s16 ip = 0;
    while(1) {
        a = subleq_read(ip); ip++;
        b = subleq_read(ip); ip++;
        c = subleq_read(ip); ip++;
        if(a == -1 && b == -1 && c == -1) break;
        r = subleq_read(b) - subleq_read(a);
        subleq_write(b, r);
        if(r <= 0) ip = c;
    }
}

*/