# Initial Settings — AAOS

Código Kotlin para integração na árvore existente do Car Settings. Não é um APK independente: depende de BaseFragment, R.xml.homepage_fragment e do tema do aplicativo existente.

## Arquivos

- InitialSettingsFragment: contexto temático, inflação XML, snapshots, ícones e lifecycle.
- InitialSettingsViewModel: estado com escopo do Fragment, sem referências Android de UI.
- GetSettingsCardsUseCase: filtra visibilidade e preserva ordem.
- SettingsCardsAdapter: cards horizontais, DiffUtil, clique e atualização de ícones.
- res/: layouts e background ilustrativos.

## Integração

1. Copie src/ e res/ para o módulo vendor do Car Settings; ajuste package e import R se necessário.
2. Inclua os fontes Kotlin no Android.bp existente. O módulo precisa de AndroidX Fragment, Lifecycle ViewModel, lifecycle-runtime-ktx (repeatOnLifecycle), Preference, RecyclerView e kotlinx-coroutines. Os nomes dos módulos Soong variam conforme a árvore; não há Android.bp fictício neste projeto.
3. A Activity deve implementar InitialSettingsNavigator.openLegacySettings(key), usando sua transição existente para abrir a interface antiga e selecionar a categoria. Preserve o retorno à initial screen; não limpe sua entrada ao trocar categorias.
4. O tema da Activity deve definir androidx.preference preferenceTheme. createThemedContext usa esse atributo e aplica ContextThemeWrapper.
5. Confirme o XML usado pela homepage da sua branch. Os grupos são percorridos, mas PreferenceScreen é tratado como destino. Somente folhas/destinos com key entram na lista.

## Limitações explícitas

Esta entrega implementa a apresentação e leitura do XML. NÃO inicializa os PreferenceControllers do AAOS. Assim, não garante disponibilidade dinâmica, itens injetados, dependências de Preferences, atualizações de summary ou equivalência às restrições de direção da interface antiga.

Para integrar controllers, replique o ciclo de vida suportado pelo SettingsFragment da branch: criação com contexto temático/FragmentController/restrições, associação à hierarquia, registro no lifecycle, encaminhamento das UX restrictions e limpeza. Publique mudanças via refreshCards() na main thread. Essa integração precisa conhecer a versão do AAOS e não é substituída pelo adapter.

inflateFromResource é API restrita AndroidX, explicitamente suprimida no loader. Confirme as regras de lint da árvore. Este código não é uma recomendação para apps externos ao Settings.

Os recursos visuais são exemplos. Integre Car UI/rotary, áreas de foco e restrições com a infraestrutura do vendor. O BaseFragment mantém seu comportamento de canBeShown; não foi liberado uso em movimento.

## Verificação

Não compilado contra uma árvore AAOS. Validar build/lint, cards e ordem com overlays, rotação/recriação, retorno por Back, abertura por key, estados dinâmicos, foco/rotary e UX restrictions na branch alvo.

Fontes públicas de referência:
- https://android.googlesource.com/platform/packages/apps/Car/Settings/+/refs/heads/main/src/com/android/car/settings/common/SettingsFragment.java
- https://android.googlesource.com/platform/packages/apps/Car/Settings/+/refs/heads/master/src/com/android/car/settings/common/BaseFragment.java
