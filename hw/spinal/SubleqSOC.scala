import scala.math

import spinal.core._

case class SubleqSOC() extends Component {
    val io = new Bundle {

    }

    val cfg = SubleqConfig(
        width = 16
    )

    val ramSize = math.pow(2, cfg.width).toInt
    val mem = Mem(SInt(cfg.width bits), ramSize)

    val subleq = Subleq(cfg)


    val sb_addr = subleq.io.bus.addr
    val sb_wdata = subleq.io.bus.wdata
    val sb_write = subleq.io.bus.write


    val is_mmio_rq = sb_addr < 0

    
    when(is_mmio_rq) {
        subleq.io.bus.rdata := 0
    } otherwise {
        mem.write(
            address = sb_addr.asUInt,
            data    = sb_wdata,
            enable  = sb_write
        )

        subleq.io.bus.rdata := mem.readAsync(
            address = sb_addr.asUInt
        )
    }
}