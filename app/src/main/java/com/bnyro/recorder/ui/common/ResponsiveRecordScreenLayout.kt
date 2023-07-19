package com.bnyro.recorder.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun ResponsiveRecordScreenLayout(
    modifier: Modifier = Modifier,
    PaneOne: @Composable () -> Unit,
    PaneTwo: @Composable () -> Unit
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    ConstraintLayout(modifier) {
        val (panel1, panel2) = createRefs()

        val panel1Modifier =
            if (isPortrait) {
                Modifier
                    .constrainAs(panel1) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(panel2.top)
                        height = Dimension.fillToConstraints
                    }
            } else {
                Modifier
                    .constrainAs(panel1) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(panel2.start)
                        width = Dimension.fillToConstraints
                    }
            }

        val panel2Modifier =
            if (isPortrait) {
                Modifier.padding(bottom = 20.dp)
                    .constrainAs(panel2) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(panel1.bottom)
                        bottom.linkTo(parent.bottom)
                    }
            } else {
                Modifier.padding(start = 50.dp, end = 30.dp)
                    .constrainAs(panel2) {
                        start.linkTo(panel1.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            }

        Box(
            modifier = panel1Modifier
        ) {
            PaneOne()
        }
        Box(
            modifier = panel2Modifier
        ) {
            PaneTwo()
        }
    }
}
