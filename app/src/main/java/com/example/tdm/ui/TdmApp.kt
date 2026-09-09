package com.example.tdm.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tdm.model.ThemeMode
import com.example.tdm.ui.screens.*
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdmApp(
  viewModel: TdmViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val activeTab by viewModel.activeTab.collectAsState()
  val themeMode by viewModel.themeMode.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val canGoBack by viewModel.canGoBack.collectAsState()
  BackHandler(enabled = drawerState.isOpen || canGoBack) {
    if (drawerState.isOpen) scope.launch { drawerState.close() }
    else viewModel.goBack()
  }

  LaunchedEffect(Unit) {
    viewModel.userMessage.collectLatest { msg ->
      snackbarHostState.showSnackbar(
        message = msg,
        duration = SnackbarDuration.Short
      )
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = true,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .widthIn(max = 330.dp)
          .testTag("side_bar_drawer_sheet"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 6.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Side Bar Header
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("drawer_header_banner")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.Vaccines,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "TDM Insight",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                IconButton(
                  onClick = { scope.launch { drawerState.close() } },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Drawer",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }

              Text(
                text = "Vancomycin Pharmacokinetics Decision Support",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )

              Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "ASHP/IDSA/PIDS/SIDP 2020 Guidelines",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Primary Section: Side Bar Features (Explanation, Simulation, History)
          Text(
            text = "ANALYTICS & TOOLS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )

          // 1. Explanation
          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.EXPLANATION) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                contentDescription = null
              )
            },
            label = {
              Column {
                Text("Explanation", fontWeight = FontWeight.SemiBold)
                Text(
                  "Step-by-step PK formulas & derivation",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            selected = activeTab == AppTab.EXPLANATION,
            onClick = {
              viewModel.setTab(AppTab.EXPLANATION)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_explanation")
          )

          // 2. Simulation
          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.SIMULATOR) Icons.Filled.Science else Icons.Outlined.Science,
                contentDescription = null
              )
            },
            label = {
              Column {
                Text("Simulation", fontWeight = FontWeight.SemiBold)
                Text(
                  "What-If regimen titration & AUC curve",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            selected = activeTab == AppTab.SIMULATOR,
            onClick = {
              viewModel.setTab(AppTab.SIMULATOR)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_simulator")
          )

          // 3. History
          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = null
              )
            },
            label = {
              Column {
                Text("History", fontWeight = FontWeight.SemiBold)
                Text(
                  "Local patient records & scanned slips",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            selected = activeTab == AppTab.HISTORY,
            onClick = {
              viewModel.setTab(AppTab.HISTORY)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_history")
          )

          Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

          // Secondary Section: Quick Access to Core Workflows
          Text(
            text = "CORE CLINICAL WORKFLOWS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )

          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.CALCULATOR) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                contentDescription = null
              )
            },
            label = { Text("Dose Calculator") },
            selected = activeTab == AppTab.CALCULATOR,
            onClick = {
              viewModel.setTab(AppTab.CALCULATOR)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_calculator")
          )

          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.CAMERA) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                contentDescription = null
              )
            },
            label = { Text("Lab Camera Scanner") },
            selected = activeTab == AppTab.CAMERA,
            onClick = {
              viewModel.setTab(AppTab.CAMERA)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_camera")
          )

          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.RESULTS) Icons.Filled.Insights else Icons.Outlined.Insights,
                contentDescription = null
              )
            },
            label = { Text("PK Results & Curve") },
            selected = activeTab == AppTab.RESULTS,
            onClick = {
              viewModel.setTab(AppTab.RESULTS)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_results")
          )

          Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

          NavigationDrawerItem(
            icon = {
              Icon(
                imageVector = if (activeTab == AppTab.DISCLAIMER) Icons.Filled.Policy else Icons.Outlined.Policy,
                contentDescription = null
              )
            },
            label = { Text("Clinical Disclaimer") },
            selected = activeTab == AppTab.DISCLAIMER,
            onClick = {
              viewModel.setTab(AppTab.DISCLAIMER)
              scope.launch { drawerState.close() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("drawer_item_disclaimer")
          )

          Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

          // Theme / Appearance Section
          Text(
            text = "APPEARANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )

          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("theme_selector_container")
          ) {
            Column(
              modifier = Modifier.padding(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              ThemeMode.values().forEach { mode ->
                val isSelected = themeMode == mode
                Surface(
                  onClick = { viewModel.setThemeMode(mode) },
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_option_${mode.name.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Icon(
                      imageVector = when (mode) {
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                      },
                      contentDescription = null,
                      tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = mode.iconDescription,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.weight(1f))

          // Footer info
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "Albukhary International University",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "CDE2313 Mobile Application Development • For Academic & Training Evaluation",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  ) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .testTag("tdm_app_root"),
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        CenterAlignedTopAppBar(
          navigationIcon = {
            IconButton(
              onClick = { scope.launch { drawerState.open() } },
              modifier = Modifier.testTag("open_side_bar_button")
            ) {
              Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Open Side Bar Menu",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          },
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Vaccines,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "TDM Insight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "Vancomycin",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          },
          actions = {
            val systemDark = isSystemInDarkTheme()
            val isCurrentDark = when (themeMode) {
              ThemeMode.SYSTEM -> systemDark
              ThemeMode.LIGHT -> false
              ThemeMode.DARK -> true
            }

            IconButton(
              onClick = { viewModel.toggleTheme() },
              modifier = Modifier.testTag("theme_mode_toggle_button")
            ) {
              Icon(
                imageVector = if (isCurrentDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isCurrentDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = MaterialTheme.colorScheme.primary
              )
            }

            IconButton(
              onClick = { viewModel.setTab(AppTab.DISCLAIMER) },
              modifier = Modifier.testTag("disclaimer_appbar_button")
            ) {
              Icon(
                imageVector = if (activeTab == AppTab.DISCLAIMER) Icons.Filled.Info else Icons.Outlined.Info,
                contentDescription = "Clinical Disclaimer & References",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          },
          colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )
      },
      bottomBar = {
        // Bottom Navigation Bar with core 3 calculation workflows
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          val bottomNavItems = listOf(
            Triple(AppTab.CALCULATOR, Icons.Filled.Calculate, Icons.Outlined.Calculate),
            Triple(AppTab.CAMERA, Icons.Filled.PhotoCamera, Icons.Outlined.PhotoCamera),
            Triple(AppTab.RESULTS, Icons.Filled.Insights, Icons.Outlined.Insights)
          )

          bottomNavItems.forEach { (tab, filledIcon, outlinedIcon) ->
            val isSelected = activeTab == tab
            NavigationBarItem(
              selected = isSelected,
              onClick = { viewModel.setTab(tab) },
              icon = {
                Icon(
                  imageVector = if (isSelected) filledIcon else outlinedIcon,
                  contentDescription = tab.title
                )
              },
              label = {
                Text(
                  text = tab.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
              ),
              modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
          }
        }
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(MaterialTheme.colorScheme.background)
      ) {
        when (activeTab) {
          AppTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
          AppTab.CAMERA -> CameraScreen(viewModel = viewModel)
          AppTab.RESULTS -> ResultsScreen(viewModel = viewModel)
          AppTab.EXPLANATION -> ExplanationScreen(viewModel = viewModel)
          AppTab.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
          AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
          AppTab.DISCLAIMER -> DisclaimerScreen()
        }
      }
    }
  }
}

