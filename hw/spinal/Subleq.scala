import spinal.core._
import spinal.lib._
import spinal.lib.fsm._


case class SubleqConfig(
    val width: Int
) {
    def dtype() = SInt(width bits)
    def reg() = Reg(dtype()) init(0)
}


case class SubleqBus(cfg: SubleqConfig) extends Bundle {
    val addr = out(cfg.dtype())
    val rdata = in(cfg.dtype())
    val wdata = out(cfg.dtype())
    val write = out(Bool())
    val ready = in(Bool())
}


case class Subleq(cfg: SubleqConfig) extends Component {
    val io = new Bundle {
        val bus = SubleqBus(cfg)
    }


    val regs = new Area {
        val a = cfg.reg()
        val b = cfg.reg()
        val c = cfg.reg()
        val ip = cfg.reg()
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


    val sub = t1 - t0
    val br = sub <= 0


    val state = Reg(UInt(5 bits)) init(0)
    val b_wait = Bool()
    b_wait := True
    
    var stateID = 0
    def nextStateID(): Int = {
        var id = stateID
        stateID += 1
        return id
    }

    switch(state) {
        def nextState(body: => Unit) = is(nextStateID())(body)

        def defFetchState(addr: SInt, reg: SInt, incIP: Boolean = true) {
            nextState {
                b_wait := True
                b_addr := addr
            }
            nextState(reg := io.bus.rdata)
            nextState {
                b_wait := False
                if(incIP) regs.ip := regs.ip + 1
            }
        }

        defFetchState(regs.ip, regs.a)
        defFetchState(regs.ip, regs.b)
        defFetchState(regs.ip, regs.c)
        defFetchState(regs.a, t0, false)
        defFetchState(regs.b, t1, false)

        nextState {
            b_wait := True
            b_addr := regs.b
            b_wdata := sub
            b_write := True
        }

        nextState {
            b_wait := False
            b_write := False
            when(br) {
                regs.ip := regs.c
            }
        }
    }

    when(!b_wait | io.bus.ready) {
        when(state === stateID) {
            state := 0
        } otherwise {
            state := state + 1
        }
    }
}